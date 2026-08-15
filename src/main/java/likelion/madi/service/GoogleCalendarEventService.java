package likelion.madi.service;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleCalendarEventsResponseDto;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarEventService {

    private final UserRepository userRepository;
    private final GoogleCalendarConnectionRepository calendarRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String clientSecret;

    @Transactional
    public GoogleCalendarEventsResponseDto getCalendarEvents(Long userId, String startDateStr, String endDateStr) {
        // 1. 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다. ID: " + userId));

        // 2. 구글 캘린더 연동 여부 확인 (3번 해결: 500 대신 404 NOT_FOUND 반환)
        Optional<GoogleCalendarConnection> connectionOpt = calendarRepository.findByUser(user);

        // 연동 정보가 아예 없거나, 토큰이 비어있는(해제된) 경우
        if (connectionOpt.isEmpty() ||
                connectionOpt.get().getAccessToken() == null ||
                connectionOpt.get().getAccessToken().isBlank()) {

            log.info("유저 ID [{}]는 캘린더 미연동/해제 상태입니다. 빈 배열을 반환합니다.", userId);

            // 404/400 에러를 던지지 않고 schedules에 빈 배열([])을 담아 200 정상 응답 처리
            return GoogleCalendarEventsResponseDto.builder()
                    .userId(userId)
                    .schedules(new ArrayList<>())
                    .build();
        }
        GoogleCalendarConnection connection = connectionOpt.get();

        // 3. 날짜 파싱 및 검증 (날짜 역전 체크)
        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = (startDateStr == null || startDateStr.isBlank())
                    ? LocalDate.now().withDayOfMonth(1)
                    : LocalDate.parse(startDateStr);

            endDate = (endDateStr == null || endDateStr.isBlank())
                    ? YearMonth.now().atEndOfMonth()
                    : LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD 권장)");
        }

        // ⭐ startDate가 endDate보다 늦은 경우 400 에러 처리
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 날짜(startDate)는 종료 날짜(endDate)보다 늦을 수 없습니다.");
        }

        log.info("Google Calendar Fetch Range: {} ~ {}", startDate, endDate);

        // 4. 구글 API 호출 (1번 해결: 만료 시 RefreshToken 자동 갱신 적용)
        List<GoogleCalendarEventsResponseDto.ScheduleItem> scheduleItems = fetchGoogleEventsWithAutoRefresh(connection, startDate, endDate);

        return GoogleCalendarEventsResponseDto.builder()
                .userId(userId)
                .schedules(scheduleItems)
                .build();
    }

    /**
     * 일정 조회 및 토큰 만료 시 RefreshToken 자동 갱신 로직
     */
    private List<GoogleCalendarEventsResponseDto.ScheduleItem> fetchGoogleEventsWithAutoRefresh(
            GoogleCalendarConnection connection, LocalDate start, LocalDate end) {

        try {
            return requestGoogleCalendarEvents(connection.getAccessToken(), start, end);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("구글 AccessToken 만료 감지(401). RefreshToken으로 재발급을 시도합니다.");

            if (connection.getRefreshToken() == null || connection.getRefreshToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "구글 연동 토큰이 만료되었습니다. 다시 연동해주세요.");
            }

            // 구글 서버에 refresh_token을 보내 새로운 access_token 획득
            String newAccessToken = refreshGoogleAccessToken(connection.getRefreshToken());

            // 엔티티 업데이트 (Dirty Checking을 통해 DB에 반영)
            connection.updateTokens(newAccessToken, connection.getRefreshToken());
            calendarRepository.save(connection);

            // 갱신된 토큰으로 재호출
            return requestGoogleCalendarEvents(newAccessToken, start, end);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Google Calendar API 호출 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "구글 캘린더 일정을 가져오는 중 오류가 발생했습니다.");
        }
    }

    /**
     * 구글 OAuth 서버에 RefreshToken을 전달하여 새로운 AccessToken을 발급받는 메서드
     */
    private String refreshGoogleAccessToken(String refreshToken) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("refresh_token", refreshToken);
            params.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                return (String) response.getBody().get("access_token");
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "새로운 Access Token 발급에 실패했습니다.");
        } catch (Exception e) {
            log.error("Google Token Refresh 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "구글 인증이 만료되었습니다. 다시 연동해주세요.");
        }
    }

    /**
     * Google Calendar v3 API 실제 HTTP 요청 및 매핑
     */
    private List<GoogleCalendarEventsResponseDto.ScheduleItem> requestGoogleCalendarEvents(String accessToken, LocalDate start, LocalDate end) {
        List<GoogleCalendarEventsResponseDto.ScheduleItem> items = new ArrayList<>();

        String timeMin = start.atStartOfDay(ZoneId.of("Asia/Seoul"))
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        String timeMax = end.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul"))
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                .queryParam("timeMin", timeMin)
                .queryParam("timeMax", timeMax)
                .queryParam("singleEvents", true)
                .queryParam("orderBy", "startTime")
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("items")) {
            List<Map<String, Object>> rawEvents = (List<Map<String, Object>>) response.getBody().get("items");
            long scheduleId = 1L;

            for (Map<String, Object> event : rawEvents) {
                String title = (String) event.getOrDefault("summary", "(제목 없음)");
                String location = (String) event.getOrDefault("location", "");

                Map<String, Object> startMap = (Map<String, Object>) event.get("start");
                String eventDate = "";
                String eventTime = "00:00:00";

                if (startMap != null) {
                    if (startMap.containsKey("dateTime")) {
                        String dateTimeStr = (String) startMap.get("dateTime");
                        if (dateTimeStr.length() >= 19) {
                            eventDate = dateTimeStr.substring(0, 10);
                            eventTime = dateTimeStr.substring(11, 19);
                        }
                    } else if (startMap.containsKey("date")) {
                        eventDate = (String) startMap.get("date");
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

        return items;
    }
}