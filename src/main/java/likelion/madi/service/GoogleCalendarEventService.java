package likelion.madi.service;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleCalendarEventsResponseDto;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarEventService {

    private final UserRepository userRepository;
    private final GoogleCalendarConnectionRepository calendarRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public GoogleCalendarEventsResponseDto getCalendarEvents(Long userId, String startDateStr, String endDateStr) {
        // 1. 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. 구글 캘린더 연동 상태 및 토큰 확인
        GoogleCalendarConnection connection = calendarRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("연동된 구글 캘린더 정보가 없습니다. 먼저 캘린더를 연동해주세요."));

        // 3. 날짜 기본값 처리 (미입력 시 이번 달 1일 ~ 이번 달 말일)
        LocalDate startDate;
        LocalDate endDate;

        if (startDateStr == null || startDateStr.isBlank()) {
            startDate = LocalDate.now().withDayOfMonth(1);
        } else {
            startDate = LocalDate.parse(startDateStr);
        }

        if (endDateStr == null || endDateStr.isBlank()) {
            endDate = YearMonth.now().atEndOfMonth();
        } else {
            endDate = LocalDate.parse(endDateStr);
        }

        log.info("Google Calendar Fetch Range: {} ~ {}", startDate, endDate);

        // 4. 구글 API를 호출하여 실제 일정 받아오기
        List<GoogleCalendarEventsResponseDto.ScheduleItem> scheduleItems = fetchGoogleEvents(connection.getAccessToken(), startDate, endDate);

        return GoogleCalendarEventsResponseDto.builder()
                .userId(userId)
                .schedules(scheduleItems)
                .build();
    }

    /**
     * Google Calendar v3 API 실제 호출 및 일정 매핑
     */
    private List<GoogleCalendarEventsResponseDto.ScheduleItem> fetchGoogleEvents(String accessToken, LocalDate start, LocalDate end) {
        List<GoogleCalendarEventsResponseDto.ScheduleItem> items = new ArrayList<>();

        try {
            // 구글 캘린더가 100% 인식하는 ISO 8601 UTC 'Z' 포맷으로 변환 (400 Bad Request 에러 해결)
            String timeMin = start.atStartOfDay(ZoneId.of("Asia/Seoul"))
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            String timeMax = end.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul"))
                    .withZoneSameInstant(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            // URL 생성 (queryParam을 구글 권장 규격으로 빌드)
            String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                    .queryParam("timeMin", timeMin)
                    .queryParam("timeMax", timeMax)
                    .queryParam("singleEvents", true)
                    .queryParam("orderBy", "startTime")
                    .build()
                    .toUriString();

            log.info("Request Google Calendar API URL: {}", url);

            // HTTP 헤더 설정 (Authorization: Bearer <AccessToken>)
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("items")) {
                List<Map<String, Object>> rawEvents = (List<Map<String, Object>>) response.getBody().get("items");
                long scheduleId = 1L;

                for (Map<String, Object> event : rawEvents) {
                    String title = (String) event.getOrDefault("summary", "(제목 없음)");
                    String location = (String) event.getOrDefault("location", "");

                    // 시작 시간 파싱
                    Map<String, Object> startMap = (Map<String, Object>) event.get("start");
                    String eventDate = "";
                    String eventTime = "00:00:00";

                    if (startMap != null) {
                        if (startMap.containsKey("dateTime")) { // 특정 시간 일정
                            String dateTimeStr = (String) startMap.get("dateTime"); // 예: 2026-08-15T14:00:00+09:00
                            eventDate = dateTimeStr.substring(0, 10);
                            eventTime = dateTimeStr.substring(11, 19);
                        } else if (startMap.containsKey("date")) { // 하루 종일 일정
                            eventDate = (String) startMap.get("date"); // 예: 2026-08-15
                            eventTime = "00:00:00";
                        }
                    }

                    items.add(GoogleCalendarEventsResponseDto.ScheduleItem.builder()
                            .scheduleId(scheduleId++)
                            .title(title)
                            .eventDate(eventDate)
                            .eventTime(eventTime)
                            .location(location)
                            .source("google")
                            .latitude("")
                            .longitude("")
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Google Calendar API 호출 중 에러 발생: {}", e.getMessage());
            throw new IllegalStateException("구글 캘린더 연동 정보가 만료되었거나 일정을 가져올 수 없습니다.");
        }

        return items;
    }
}