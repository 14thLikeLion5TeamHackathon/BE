package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.domain.*;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.TodayCareMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayCareService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            당신은 피부 시술 후 회복 케어를 돕는 어시스턴트입니다.
            아래 제공되는 '기본 회복 가이드'의 범위를 벗어나지 않는 선에서,
            오늘의 날씨와 사용자의 최근 기록을 반영해 자연스러운 한 문장짜리 케어 문구를 작성하세요.

            규칙:
            - 의학적 진단, 처방, 새로운 치료법 제안은 하지 마세요.
            - 기본 가이드에 없는 내용을 새로 만들어내지 마세요. 표현과 강조점만 오늘 상황에 맞게 조정하세요.
            - 존댓말로, 1~2문장으로만 작성하세요.
            - 마크다운이나 이모지 없이 순수 텍스트로만 작성하세요.
            """;

    private final TodayCareMessageRepository todayCareMessageRepository;
    private final CareRecordRepository careRecordRepository;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    // 요 트랜잭션은 메서드가 호출될 때마다 완전히 새로운 쓰기 가능한 트랜잭션 만들어서 실행!
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrGetTodayCare(CareCard careCard, Treatment treatment, int dDay,
                                          String city, String district) {
        LocalDate today = LocalDate.now();

        return todayCareMessageRepository.findByCareCardAndTargetDate(careCard, today)
                .map(TodayCareMessage::getGeneratedText)
                .orElseGet(() -> generateAndCache(careCard, treatment, dDay, city, district, today));
    }

    private String generateAndCache(CareCard careCard, Treatment treatment, int dDay,
                                    String city, String district, LocalDate today) {
        String fallback = "오늘의 케어 정보를 불러오지 못했어요. 잠시 후 다시 확인해주세요.";

        try {
            String prompt = buildPrompt(careCard, treatment, dDay, city, district);
            String generated = callOpenAi(prompt);

            todayCareMessageRepository.save(TodayCareMessage.builder()
                    .careCard(careCard)
                    .targetDate(today)
                    .generatedText(generated)
                    .build());

            return generated;
        } catch (Exception e) {
            log.error("오늘의 케어 AI 생성 실패, 기본 문구로 대체합니다.", e);
            return fallback;
        }
    }
    private String buildPrompt(CareCard careCard, Treatment treatment, int dDay,
                               String city, String district) {
        WeatherResponseDto weather = weatherService.getWeatherAndEnvironment(LocalDate.now(), city, district);
        List<CareRecord> recentRecords = careRecordRepository.findTop3ByCareCardOrderByRecordedAtDesc(careCard);

        StringBuilder sb = new StringBuilder();
        sb.append("시술명: ").append(treatment != null ? treatment.getName() : "미등록 시술").append("\n");
        sb.append("경과: D+").append(dDay).append("\n\n");

        sb.append("오늘 날씨: ").append(weather.getWeatherCondition())
                .append(", 기온 ").append(weather.getTemperature()).append("도")
                .append(", 자외선지수 ").append(weather.getUvIndex())
                .append(", 미세먼지 ").append(weather.getPm10Status()).append("\n\n");

        if (!recentRecords.isEmpty()) {
            sb.append("최근 사용자 기록:\n");
            for (CareRecord record : recentRecords) {
                String desc = record.getStatusDescription() != null ? record.getStatusDescription() : "설명 없음";
                sb.append("- D+").append(record.getDDay()).append(": ").append(desc).append("\n");
            }
            sb.append("\n");
        }

        sb.append("위 정보를 참고해서 오늘 실천할 수 있는 케어 문구를 작성해주세요.");
        return sb.toString();
    }

    private String callOpenAi(String userPrompt) {
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