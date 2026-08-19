package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.domain.*;
import likelion.madi.dto.response.BriefingResponse;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.enums.ConnectionStatus;
import likelion.madi.repository.BriefingCacheRepository;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            당신은 피부 시술 회복 관리를 돕는 어시스턴트입니다.
            아래로 주어지는 진행중인 시술 카드들, 오늘 날씨/환경지표, (있다면) 오늘 일정을
            종합해서 오늘 하루에 필요한 행동을 한 문장으로 작성하세요.

            여러 카드가 있으면 전부 반영하되, 특별한 주의사항이 없는 카드는 굳이 언급하지 말고
            주의가 필요한 내용 위주로 자연스럽게 한 두 문장으로 작성하세요.
            카드가 하나뿐이면 그 카드 내용만으로 작성하세요.
            일정 정보가 주어지지 않으면 일정 얘기는 하지 마세요.

            일정 제목이나 장소로 보아 야외 활동(외출, 나들이, 결혼식, 운동, 여행 등)으로 보이면
            오늘의 자외선 지수를 반드시 함께 고려해서 자외선 차단 관련 안내를 포함하세요.
            실내 활동(회의, 카페 등)으로 보이면 자외선 언급은 생략해도 됩니다.

            반드시 아래 JSON 형식으로만 응답하세요. 마크다운이나 설명 없이 순수 JSON만 출력하세요.
            {
              "actionSentence": "오늘 필요한 행동 1-2문장",
              "cautionLevel": "낮음 또는 주의 또는 경고 중 하나",
              "reasons": ["판단 근거를 짧게 요약한 태그들"]
            }
            """;

    private final CareCardRepository careCardRepository;
    private final ScheduleRepository scheduleRepository;
    private final GoogleCalendarConnectionRepository calendarConnectionRepository;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CareRecordRepository careRecordRepository;
    private final BriefingCacheRepository briefingCacheRepository;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    // 발송/조회 쪽에서 나중에 예외가 나서 그 트랜잭션이 롤백되더라도, 방금 생성한 캐시(특히 OpenAI 호출 결과)까지
    // 같이 날아가지 않도록 별도 트랜잭션으로 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BriefingResponse getBriefing(User user, LocalDate date, String city, String district) {
        WeatherResponseDto weather = weatherService.getWeatherAndEnvironment(date, city, district);

        boolean calendarConnected = calendarConnectionRepository.findByUser(user)
                .map(conn -> conn.getStatus() == ConnectionStatus.CONNECTED)
                .orElse(false);

        // 직접 입력한 일정(MANUAL)은 캘린더 연동 여부랑 상관없이 항상 반영
        List<Schedule> schedules = scheduleRepository.findByUserAndEventDate(user, date);

        List<CareCard> cards = careCardRepository.findByUser(user).stream()
                .filter(card -> isInProgress(card, date))
                .toList();

        List<BriefingResponse.ScheduleItem> scheduleItems = schedules.stream()
                .map(s -> BriefingResponse.ScheduleItem.builder()
                        .scheduleId(s.getScheduleId())
                        .title(s.getTitle())
                        .time(s.getEventTime() != null ? s.getEventTime().toString() : null)
                        .location(s.getLocation())
                        .build())
                .toList();

        if (cards.isEmpty()) {
            return BriefingResponse.builder()
                    .date(date.toString())
                    .schedules(scheduleItems)
                    .cardJudgement(null)
                    .overallCautionLevel(null)
                    .calendarConnected(calendarConnected)
                    .build();
        }

        LocalDateTime latestRecordAt = cards.stream()
                .map(careRecordRepository::findTopByCareCardOrderByRecordedAtDesc)
                .flatMap(Optional::stream)
                .map(CareRecord::getRecordedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        BriefingResponse.CardJudgement judgement = briefingCacheRepository
                .findByUserAndTargetDateAndCityAndDistrictAndLatestRecordAt(user, date, city, district, latestRecordAt)
                .map(this::toCardJudgement)
                .orElseGet(() -> {
                    String prompt = buildPrompt(cards, weather, schedules, date);
                    BriefingResponse.CardJudgement generated = callOpenAiForJudgement(cards, prompt);
                    saveCache(user, date, city, district, latestRecordAt, generated);
                    return generated;
                });

        return BriefingResponse.builder()
                .date(date.toString())
                .schedules(scheduleItems)
                .cardJudgement(judgement)
                .overallCautionLevel(judgement.getCautionLevel())
                .calendarConnected(calendarConnected)
                .build();
    }

    private BriefingResponse.CardJudgement toCardJudgement(BriefingCache cache) {
        List<Long> cardIds = cache.getCardIds().isBlank()
                ? List.of()
                : java.util.Arrays.stream(cache.getCardIds().split(","))
                        .map(Long::parseLong)
                        .toList();

        List<String> reasons = cache.getReasons().isBlank()
                ? List.of()
                : java.util.Arrays.asList(cache.getReasons().split(","));

        return BriefingResponse.CardJudgement.builder()
                .cardIds(cardIds)
                .actionSentence(cache.getActionSentence())
                .cautionLevel(cache.getCautionLevel())
                .reasons(reasons)
                .build();
    }

    private void saveCache(User user, LocalDate date, String city, String district, LocalDateTime latestRecordAt,
                            BriefingResponse.CardJudgement judgement) {
        String cardIds = judgement.getCardIds().stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        String reasons = String.join(",", judgement.getReasons());

        briefingCacheRepository.save(
                BriefingCache.builder()
                        .user(user)
                        .targetDate(date)
                        .city(city)
                        .district(district)
                        .latestRecordAt(latestRecordAt)
                        .cardIds(cardIds)
                        .actionSentence(judgement.getActionSentence())
                        .cautionLevel(judgement.getCautionLevel())
                        .reasons(reasons)
                        .build()
        );
    }

    private String buildPrompt(List<CareCard> cards, WeatherResponseDto weather,
                                List<Schedule> schedules, LocalDate date) {
        StringBuilder sb = new StringBuilder();

        sb.append("진행중인 시술 카드:\n");
        for (CareCard card : cards) {
            CareCardTreatment primary = card.getTreatments().get(0);
            String treatmentName = primary.getTreatment() != null
                    ? primary.getTreatment().getName()
                    : primary.getCustomName();
            int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), date);
            sb.append("- ").append(treatmentName).append(" D+").append(dDay).append("\n");

            careRecordRepository.findTopByCareCardOrderByRecordedAtDesc(card).ifPresent(record -> {
                sb.append("  최근 기록: ");
                for (CareRecordTag tag : record.getTags()) {
                    sb.append(tag.getStatusTag().getName()).append(" 강도").append(tag.getIntensity()).append(" ");
                }
                if (record.getStatusDescription() != null && !record.getStatusDescription().isBlank()) {
                    sb.append("(메모: ").append(record.getStatusDescription()).append(")");
                }
                sb.append("\n");
            });
        }

        sb.append("\n오늘(").append(date).append(") 날씨: ")
                .append(weather.getWeatherCondition())
                .append(", 자외선지수 ").append(weather.getUvIndex())
                .append(", 미세먼지 ").append(weather.getPm10Status()).append("\n");

        if (!schedules.isEmpty()) {
            sb.append("\n오늘 일정:\n");
            for (Schedule schedule : schedules) {
                sb.append("- ").append(schedule.getTitle()).append(" ").append(schedule.getEventTime()).append("\n");
            }
        }

        sb.append("\n위 정보를 참고해서 지정된 JSON 형식으로만 응답하세요.");
        return sb.toString();
    }

    private BriefingResponse.CardJudgement callOpenAiForJudgement(List<CareCard> cards, String userPrompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "max_tokens", 300,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        List<Long> cardIds = cards.stream().map(CareCard::getCardId).toList();

        try {
            String response = restTemplate.postForObject(OPENAI_URL, request, String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);

            List<String> reasons = new ArrayList<>();
            parsed.path("reasons").forEach(node -> reasons.add(node.asText()));

            return BriefingResponse.CardJudgement.builder()
                    .cardIds(cardIds)
                    .actionSentence(parsed.path("actionSentence").asText())
                    .cautionLevel(parsed.path("cautionLevel").asText())
                    .reasons(reasons)
                    .build();
        } catch (Exception e) {
            log.error("브리핑 AI 생성 실패", e);
            return BriefingResponse.CardJudgement.builder()
                    .cardIds(cardIds)
                    .actionSentence("오늘의 케어 정보를 불러오지 못했어요. 잠시 후 다시 확인해주세요.")
                    .cautionLevel("낮음")
                    .reasons(List.of())
                    .build();
        }
    }

    // 진행중인 카드인지 판단 (회복 총 기간을 넘지 않았는지)
    private boolean isInProgress(CareCard card, LocalDate date) {
        CareCardTreatment primary = card.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        if (treatment == null || treatment.getRecoveryTotalDays() == null) {
            return true;
        }
        int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), date);
        return dDay >= 0 && dDay <= treatment.getRecoveryTotalDays();
    }
}
