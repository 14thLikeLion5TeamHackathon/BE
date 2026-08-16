package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.Schedule;
import likelion.madi.domain.Treatment;
import likelion.madi.common.util.KstDate;
import likelion.madi.domain.User;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
           당신은 피부 시술 회복 관리 위험을 감지하는 어시스턴트입니다.
            내일 일정과 시술 D-day 조합을 보고 위험한 조합(예: 음주 일정 + D+3 → 붓기 악화)이 있으면
            1~2문장 경고 문구를 작성하세요. 위험한 조합이 없으면 다른 말 없이 정확히 NONE 이라고만 답하세요.
            """;

    private final ScheduleRepository scheduleRepository;
    private final CareCardRepository careCardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public Optional<String> checkTomorrowRisk(User user) {
        LocalDate tomorrow = KstDate.today().plusDays(1);

        List<Schedule> schedules = scheduleRepository.findByUserAndEventDate(user, tomorrow);
        List<CareCard> cards = careCardRepository.findByUser(user);

        String prompt = buildPrompt(schedules, cards, tomorrow);
        String result = callOpenAi(prompt);

        if (result.equals("NONE")) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    private String buildPrompt(List<Schedule> schedules, List<CareCard> cards, LocalDate tomorrow) {
        StringBuilder sb = new StringBuilder();

        sb.append("내일(").append(tomorrow).append(") 일정:\n");
        if (schedules.isEmpty()) {
            sb.append("일정 없음\n");
        } else {
            for (Schedule schedule : schedules) {
                sb.append("- ").append(schedule.getTitle()).append(" ").append(schedule.getEventTime()).append("\n");
            }
        }

        sb.append("\n진행중인 시술 정보:\n");
        for (CareCard card : cards) {
            CareCardTreatment primary = card.getTreatments().get(0);
            String treatmentName = primary.getTreatment() != null
                    ? primary.getTreatment().getName()
                    : primary.getCustomName();
            int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), tomorrow);
            sb.append("- ").append(treatmentName).append(" D+").append(dDay).append("\n");
        }

        sb.append("\n위 정보를 참고해서 위험한 조합이 있는지 판단해주세요.");
        return sb.toString();
    }

    private String callOpenAi(String userPrompt) {
        // TodayCareService.callOpenAi()랑 거의 동일하게 작성하시면 돼요
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "max_tokens", 200,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(OPENAI_URL, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText().trim();
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 응답 파싱 실패", e);
        }
    }
}