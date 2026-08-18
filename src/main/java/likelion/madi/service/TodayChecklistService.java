package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.AiFeedback;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.CareChecklist;
import likelion.madi.domain.CareRecordTag;
import likelion.madi.domain.RecoveryGuide;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.response.TodayChecklistResponse;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.repository.AiFeedbackRepository;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareChecklistRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.RecoveryGuideRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayChecklistService {

    // 문장 끝 기준으로 쪼갬 (마침표, 느낌표, 물음표 등등...) - AI 생성 실패 시 폴백용
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    // 오늘의 체크리스트에 한 번에 보여줄 최대 항목 수 (케어카드가 여러 개일 때 현재 상태가 급한 카드부터 채움)
    private static final int MAX_VISIBLE_ITEMS = 3;

    // 카드 하나당 AI가 생성하는 체크리스트 항목 수
    private static final int AI_ITEM_COUNT = 3;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final Map<Integer, String> INTENSITY_LABEL = Map.of(
            0, "없음", 1, "약간", 2, "보통", 3, "심함"
    );

    private static final String SYSTEM_PROMPT = """
        당신은 피부 시술 후 회복 체크리스트를 작성하는 어시스턴트입니다.
        사용자가 오늘 실천할 수 있는 케어 행동 3가지를, 앱 체크박스 옆에 붙는 "체크리스트 항목"으로 작성하세요.

        체크리스트 항목 형식 규칙 (반드시 지키세요):
        - "명사구 + ~하기"로 끝나는 짧은 지시어 형태로만 작성하세요.
          (좋은 예: "외출 전 선크림 바르기", "미지근한 물로 순하게 세안하기", "물 충분히 마시기")
        - "~해주세요", "~합니다", "~거예요", "~시기예요" 같은 설명체·존댓말 문장, 예측·요약 문장은 절대 쓰지 마세요.
          체크박스 옆에 붙는 짧은 문구여야지, 코멘트나 안내 문장이면 안 됩니다.
        - 한 항목당 25자를 넘기지 마세요.
        - "~하지 않기", "~자제하기", "~피하기", "~말고" 같은 금지형 표현은 절대 쓰지 마세요.
          금지해야 할 행동이 있다면 그 행동을 재진술하지 말고, 사용자가 실제로 할 수 있는 다른 구체적 행동으로 완전히 바꿔서 표현하세요.
          (나쁜 예: "얼굴 만지지 말고 두기" / 좋은 예: "손 씻고 거즈로만 가볍게 관리하기")
        - 의학적 진단, 처방, 새로운 치료법 제안은 하지 마세요.
        - '기본 가이드'가 주어졌다면 그 범위를 벗어난 내용을 새로 만들어내지 말고, 표현과 강조점만 오늘 상황(회복 기록, 날씨)에 맞게 조정하세요.
        - '기본 가이드'가 주어지지 않았다면, 일반적인 시술 후 피부 관리 상식 수준에서만 작성하세요.

        나쁜 예 (이렇게 쓰면 안 됨): "피부 탄력이 서서히 개선되는 시기라 오늘은 순한 세안과 보습으로만 관리해주세요." (너무 길고 설명체)
        나쁜 예 (이렇게 쓰면 안 됨): "시술 후 2일 정도는 편안해질 거예요." (행동이 아니라 예측/요약)
        좋은 예: "순한 클렌저로 미지근하게 세안하기"

        반드시 아래 JSON 형식으로만 응답하세요. 마크다운이나 설명 없이 순수 JSON만 출력하세요.
        {
          "items": ["행동 문구 1", "행동 문구 2", "행동 문구 3"]
        }
        """;

    private final UserRepository userRepository;
    private final CareCardRepository careCardRepository;
    private final CareChecklistRepository careChecklistRepository;
    private final CareRecordRepository careRecordRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final RecoveryGuideRepository recoveryGuideRepository;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    // 조회
    @Transactional
    public TodayChecklistResponse getTodayChecklist(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        List<CareCard> careCards = careCardRepository.findByUser(user);

        // 체크리스트 항목은 카드별로 날짜마다 생성/유지되어야 하므로, 노출 여부와 무관하게 모든 카드에 대해 만들어둔다.
        Map<CareCard, List<TodayChecklistResponse.Item>> itemsByCard = new LinkedHashMap<>();
        for (CareCard careCard : careCards) {
            itemsByCard.put(careCard, buildItemsForCard(careCard, date, user));
        }

        List<CareCard> cardsByUrgency = careCards.stream()
                .sorted(Comparator.comparingInt(this::computeUrgencyScore).reversed())
                .toList();

        List<TodayChecklistResponse.Item> visibleItems = new ArrayList<>();
        for (CareCard careCard : cardsByUrgency) {
            for (TodayChecklistResponse.Item item : itemsByCard.get(careCard)) {
                if (visibleItems.size() >= MAX_VISIBLE_ITEMS) {
                    break;
                }
                visibleItems.add(item);
            }
        }

        long completedCount = visibleItems.stream().filter(TodayChecklistResponse.Item::isCompleted).count();

        return TodayChecklistResponse.builder()
                .completedCount((int) completedCount)
                .totalCount(visibleItems.size())
                .items(visibleItems)
                .build();
    }

    // 케어카드의 현재 상태 긴급도 점수: AI가 병원 문의를 권고했으면 최우선, 아니면 최근 기록의 증상 강도(0~3) 중 최댓값, 기록이 없으면 0
    private int computeUrgencyScore(CareCard careCard) {
        return careRecordRepository.findTopByCareCardOrderByRecordedAtDesc(careCard)
                .map(record -> {
                    boolean needsConsultation = aiFeedbackRepository.findByCareRecord(record)
                            .map(AiFeedback::getNeedsConsultation)
                            .orElse(false);
                    if (needsConsultation) {
                        return 100;
                    }
                    return record.getTags().stream()
                            .mapToInt(CareRecordTag::getIntensity)
                            .max()
                            .orElse(0);
                })
                .orElse(0);
    }

    // 수정 (체크/해제)
    @Transactional
    public TodayChecklistResponse.Item updateChecklist(Long userId, Long checklistId, boolean completed) {
        CareChecklist checklist = careChecklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CHECKLIST));

        CareCard careCard = checklist.getCareCard();
        if (!careCard.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        if (completed) {
            checklist.check();
        } else {
            checklist.uncheck();
        }

        String sourceLabel = resolveSourceLabel(careCard, checklist.getCheckDate());

        return TodayChecklistResponse.Item.builder()
                .checklistId(checklist.getChecklistId())
                .label(checklist.getLabel())
                .sourceLabel(sourceLabel)
                .completed(Boolean.TRUE.equals(checklist.getIsChecked()))
                .build();
    }

    private String resolveSourceLabel(CareCard careCard, LocalDate checkDate) {
        if (careCard.getTreatments().isEmpty()) {
            return null;
        }
        CareCardTreatment primary = careCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        String name = treatment != null ? treatment.getName() : primary.getCustomName();
        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), checkDate);
        return name + " D+" + dDay;
    }

    private List<TodayChecklistResponse.Item> buildItemsForCard(CareCard careCard, LocalDate today, User user) {
        if (careCard.getTreatments().isEmpty()) {
            return List.of();
        }

        CareCardTreatment primary = careCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();

        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), today);
        // 커스텀(카탈로그 미등록) 시술은 recoveryTotalDays가 없으므로 기존 관례대로 기간 제한 없이 활성 취급
        Integer recoveryTotalDays = treatment != null ? treatment.getRecoveryTotalDays() : null;
        boolean active = dDay >= 0 && (recoveryTotalDays == null || dDay <= recoveryTotalDays);
        if (!active) {
            return List.of();
        }

        // 그날 이미 생성된 체크리스트가 있으면 그대로 재사용 (하루 한 번만 AI 호출, 체크 상태 유지)
        List<CareChecklist> existing = careChecklistRepository.findByCareCardAndCheckDate(careCard, today).stream()
                .sorted(Comparator.comparing(CareChecklist::getChecklistId))
                .toList();
        if (!existing.isEmpty()) {
            String sourceLabel = resolveSourceLabel(careCard, today);
            return existing.stream()
                    .map(c -> TodayChecklistResponse.Item.builder()
                            .checklistId(c.getChecklistId())
                            .label(c.getLabel())
                            .sourceLabel(sourceLabel)
                            .completed(Boolean.TRUE.equals(c.getIsChecked()))
                            .build())
                    .toList();
        }

        List<String> labels = generateTodayCareLabels(careCard, primary, treatment, dDay, today, user);
        if (labels.isEmpty()) {
            return List.of();
        }

        String sourceLabel = resolveSourceLabel(careCard, today);
        List<TodayChecklistResponse.Item> items = new ArrayList<>();
        for (String label : labels) {
            CareChecklist checklist = careChecklistRepository.save(CareChecklist.builder()
                    .careCard(careCard)
                    .checkDate(today)
                    .label(label)
                    .build());

            items.add(TodayChecklistResponse.Item.builder()
                    .checklistId(checklist.getChecklistId())
                    .label(checklist.getLabel())
                    .sourceLabel(sourceLabel)
                    .completed(false)
                    .build());
        }
        return items;
    }

    // 시술 기준 + 회복 기록 + 날씨를 반영해 AI로 오늘의 체크리스트 문구를 생성. 실패 시 기본 가이드 문장으로 대체.
    private List<String> generateTodayCareLabels(CareCard careCard, CareCardTreatment primary, Treatment treatment,
                                                 int dDay, LocalDate today, User user) {
        String baseGuide = resolveBaseGuide(treatment, dDay);
        // 카탈로그 시술인데 기본 가이드가 전혀 없으면(today_care 미입력 등) 기존과 동일하게 생성하지 않음
        if (treatment != null && baseGuide == null) {
            return List.of();
        }

        try {
            String prompt = buildPrompt(primary, treatment, dDay, baseGuide, careCard, today, user);
            List<String> generated = callOpenAiForChecklist(prompt);
            if (!generated.isEmpty()) {
                return generated;
            }
        } catch (Exception e) {
            log.error("오늘의 체크리스트 AI 생성 실패, 기본 가이드로 대체합니다. cardId={}", careCard.getCardId(), e);
        }

        return baseGuide != null
                ? splitIntoSentences(baseGuide).stream().limit(MAX_VISIBLE_ITEMS).toList()
                : List.of();
    }

    // 시술+D-Day 구간에 맞는 RecoveryGuide를 우선 사용하고, 없으면 Treatment의 고정 오늘의 케어 텍스트로 폴백
    private String resolveBaseGuide(Treatment treatment, int dDay) {
        if (treatment == null) {
            return null;
        }

        RecoveryGuide guide = recoveryGuideRepository.findByTreatmentOrderByDDayMinAsc(treatment).stream()
                .filter(g -> dDay >= g.getDDayMin() && dDay <= g.getDDayMax())
                .findFirst()
                .orElse(null);

        if (guide != null) {
            StringBuilder sb = new StringBuilder();
            if (guide.getCareGuidance() != null) sb.append(guide.getCareGuidance()).append(" ");
            if (guide.getTodayCare() != null) sb.append(guide.getTodayCare()).append(" ");
            if (guide.getCaution() != null) sb.append(guide.getCaution());
            String combined = sb.toString().trim();
            if (!combined.isBlank()) {
                return combined;
            }
        }

        return (treatment.getTodayCare() != null && !treatment.getTodayCare().isBlank())
                ? treatment.getTodayCare()
                : null;
    }

    private String buildPrompt(CareCardTreatment primary, Treatment treatment, int dDay, String baseGuide,
                               CareCard careCard, LocalDate today, User user) {
        String name = treatment != null ? treatment.getName() : primary.getCustomName();

        StringBuilder sb = new StringBuilder();
        sb.append("시술명: ").append(name).append("\n");
        sb.append("경과: D+").append(dDay).append("\n\n");

        if (baseGuide != null) {
            sb.append("기본 가이드: ").append(baseGuide).append("\n\n");
        } else {
            sb.append("기본 가이드: 없음 (일반적인 시술 후 관리 상식 수준으로 작성)\n\n");
        }

        careRecordRepository.findTopByCareCardOrderByRecordedAtDesc(careCard).ifPresent(record -> {
            sb.append("가장 최근 기록 (D+").append(record.getDDay()).append("):\n");
            for (CareRecordTag tag : record.getTags()) {
                sb.append("- ").append(tag.getStatusTag().getName()).append(": ")
                        .append(INTENSITY_LABEL.getOrDefault(tag.getIntensity(), "알수없음")).append("\n");
            }
            if (record.getStatusDescription() != null && !record.getStatusDescription().isBlank()) {
                sb.append("- 메모: ").append(record.getStatusDescription()).append("\n");
            }
            sb.append("\n");
        });

        try {
            WeatherResponseDto weather = weatherService.getWeatherAndEnvironment(today, user.getCity(), user.getDistrict());
            sb.append("오늘 날씨: ").append(weather.getWeatherCondition())
                    .append(", 기온 ").append(weather.getTemperature()).append("도")
                    .append(", 자외선지수 ").append(weather.getUvIndex())
                    .append(", 미세먼지 ").append(weather.getPm10Status()).append("\n\n");
        } catch (Exception e) {
            log.warn("체크리스트 생성 중 날씨 조회 실패, 날씨 정보 없이 진행합니다.", e);
        }

        sb.append("위 정보를 참고해서 오늘 실천할 케어 행동 3가지를 지정된 JSON 형식으로만 응답하세요.");
        return sb.toString();
    }

    private List<String> callOpenAiForChecklist(String userPrompt) {
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
        String response = restTemplate.postForObject(OPENAI_URL, request, String.class);

        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 응답 파싱 실패", e);
        }

        String content = root.path("choices").get(0).path("message").path("content").asText();
        JsonNode parsed;
        try {
            parsed = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 체크리스트 JSON 파싱 실패", e);
        }

        List<String> items = new ArrayList<>();
        parsed.path("items").forEach(node -> {
            String text = node.asText();
            if (text != null && !text.isBlank()) {
                items.add(text.trim());
            }
        });
        return items.stream().limit(AI_ITEM_COUNT).toList();
    }

    private List<String> splitIntoSentences(String text) {
        return SENTENCE_SPLIT.splitAsStream(text.trim())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}