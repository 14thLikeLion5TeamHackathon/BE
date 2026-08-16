package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.common.exception.BaseException;
import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.common.util.KstDate;
import likelion.madi.domain.AiFeedback;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.CareChecklist;
import likelion.madi.domain.CareRecord;
import likelion.madi.domain.CareRecordTag;
import likelion.madi.domain.RecoveryGuide;
import likelion.madi.domain.Treatment;
import likelion.madi.dto.response.AiFeedbackResponse;
import likelion.madi.repository.AiFeedbackRepository;
import likelion.madi.repository.CareChecklistRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.RecoveryGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final int DAILY_QUOTA = 3;

    private static final Map<String, String> TAG_TYPE_BY_NAME = Map.of(
            "부기", "swelling",
            "통증", "pain",
            "붉은기", "redness",
            "건조함", "dryness"
    );

    private static final Map<Integer, String> INTENSITY_LABEL = Map.of(
            0, "없음", 1, "약간", 2, "보통", 3, "심함"
    );

    private static final String SYSTEM_PROMPT = """
            당신은 피부 시술 후 회복 상태를 분석해주는 어시스턴트입니다.
            아래 제공되는 사실 정보(기록 비교, 체크리스트 이행률, 회복 가이드 기준)만 근거로 판단하고,
            새로운 의학적 사실을 만들어내지 마세요.

            반드시 아래 JSON 형식으로만 응답하세요. 마크다운이나 설명 없이 순수 JSON만 출력하세요.
            {
              "analysisSummary": "전체 회복 상태에 대한 1~2문장 종합 코멘트",
              "intensityReview": "기록된 강도와 메모 내용을 비교 검토하는 1~2문장 코멘트",
              "todayCare": ["오늘 실천할 케어 항목 1", "케어 항목 2"],
              "consultationMessage": "병원 문의 필요 여부에 대한 안내 문구 1~2문장",
              "aiRiskDetected": false,
              "aiRiskReason": null
            }

            aiRiskDetected는 사용자 메모에 고름, 발열, 극심한 통증 등 규칙 판정과 별개로 주의가 필요한 표현이 있을 때만 true로 하고,
            그 경우 aiRiskReason에 감지한 근거를 간단히 적으세요. 그렇지 않으면 false와 null을 쓰세요.
            """;

    private final CareRecordRepository careRecordRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final CareChecklistRepository careChecklistRepository;
    private final RecoveryGuideRepository recoveryGuideRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Transactional
    public AiFeedbackResponse generate(Long userId, Long recordId) {
        CareRecord careRecord = getOwnedCareRecord(userId, recordId);

        if (aiFeedbackRepository.findByCareRecord(careRecord).isPresent()) {
            throw new BaseException(ErrorStatus.CONFLICT_FEEDBACK_ALREADY_EXISTS);
        }

        LocalDateTime startOfDay = KstDate.today().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        if (aiFeedbackRepository.countTodayByUser(userId, startOfDay, endOfDay) >= DAILY_QUOTA) {
            throw new BaseException(ErrorStatus.TOO_MANY_REQUESTS_FEEDBACK_QUOTA_EXCEEDED);
        }

        CareCard careCard = careRecord.getCareCard();
        CareRecord previousRecord = careRecordRepository
                .findFirstByCareCardAndRecordedAtBeforeOrderByRecordedAtDesc(careCard, careRecord.getRecordedAt())
                .orElse(null);

        List<SymptomComparison> symptomComparisons = buildSymptomComparisons(careRecord, previousRecord);
        double checklistRate = calculateChecklistRate(careCard);
        int photoCount = countPhotos(careRecord, previousRecord);

        CareCardTreatment primaryTreatment = careCard.getTreatments().get(0);
        Treatment treatment = primaryTreatment.getTreatment();
        String treatmentName = treatment != null ? treatment.getName() : primaryTreatment.getCustomName();
        int dDay = careRecord.getDDay();

        RuleVerdict ruleVerdict = evaluateRuleBasedConsultation(symptomComparisons);
        RecoveryGuide matchingGuide = findMatchingGuide(treatment, dDay);

        String prompt = buildPrompt(treatmentName, dDay, symptomComparisons, careRecord.getStatusDescription(),
                checklistRate, photoCount, matchingGuide, ruleVerdict);
        AiGeneratedContent generated = callOpenAi(prompt);

        boolean needsConsultation = ruleVerdict.triggered() || generated.aiRiskDetected();
        String consultationCriteria = resolveConsultationCriteria(ruleVerdict, generated);

        try {
            AiFeedback feedback = AiFeedback.builder()
                    .careRecord(careRecord)
                    .changeSummary(generated.analysisSummary())
                    .careGuidance(String.join(" ", generated.todayCare()))
                    .comparison(objectMapper.writeValueAsString(
                            buildComparisonInfo(previousRecord, careRecord, symptomComparisons)))
                    .analysisTags(objectMapper.writeValueAsString(
                            new AiFeedbackResponse.AnalysisTags(treatmentName + " D+" + dDay, checklistRate, photoCount)))
                    .intensityReview(generated.intensityReview())
                    .todayCare(objectMapper.writeValueAsString(generated.todayCare()))
                    .needsConsultation(needsConsultation)
                    .consultationMessage(generated.consultationMessage())
                    .consultationCriteria(consultationCriteria)
                    .build();

            aiFeedbackRepository.save(feedback);

            return AiFeedbackResponse.from(feedback, careCard.getCardId(), treatmentName, objectMapper);
        } catch (Exception e) {
            throw new IllegalStateException("AI 피드백 저장에 실패했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public AiFeedbackResponse getFeedback(Long userId, Long recordId) {
        CareRecord careRecord = getOwnedCareRecord(userId, recordId);

        AiFeedback feedback = aiFeedbackRepository.findByCareRecord(careRecord)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_AI_FEEDBACK));

        CareCard careCard = careRecord.getCareCard();
        CareCardTreatment primaryTreatment = careCard.getTreatments().get(0);
        Treatment treatment = primaryTreatment.getTreatment();
        String treatmentName = treatment != null ? treatment.getName() : primaryTreatment.getCustomName();

        return AiFeedbackResponse.from(feedback, careCard.getCardId(), treatmentName, objectMapper);
    }

    private CareRecord getOwnedCareRecord(Long userId, Long recordId) {
        CareRecord careRecord = careRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_RECORD));

        if (!careRecord.getCareCard().getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }
        return careRecord;
    }

    private List<SymptomComparison> buildSymptomComparisons(CareRecord current, CareRecord previous) {
        if (previous == null) {
            return List.of();
        }

        Map<String, Integer> previousByTag = previous.getTags().stream()
                .collect(Collectors.toMap(t -> t.getStatusTag().getName(), CareRecordTag::getIntensity));

        List<SymptomComparison> result = new ArrayList<>();
        for (CareRecordTag tag : current.getTags()) {
            String name = tag.getStatusTag().getName();
            Integer previousIntensity = previousByTag.get(name);
            if (previousIntensity == null) {
                continue;
            }
            result.add(new SymptomComparison(name, TAG_TYPE_BY_NAME.getOrDefault(name, "other"),
                    previousIntensity, tag.getIntensity()));
        }
        return result;
    }

    private double calculateChecklistRate(CareCard careCard) {
        List<CareChecklist> checklists = careChecklistRepository.findByCareCard(careCard);
        if (checklists.isEmpty()) {
            return 0.0;
        }
        long checkedCount = checklists.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsChecked()))
                .count();
        return Math.round((double) checkedCount / checklists.size() * 100) / 100.0;
    }

    private int countPhotos(CareRecord current, CareRecord previous) {
        int count = current.getPhotoUrls().size();
        if (previous != null) {
            count += previous.getPhotoUrls().size();
        }
        return count;
    }

    private String firstPhotoUrl(CareRecord record) {
        List<String> photoUrls = record.getPhotoUrls();
        return photoUrls.isEmpty() ? null : photoUrls.get(0);
    }

    private RecoveryGuide findMatchingGuide(Treatment treatment, int dDay) {
        if (treatment == null) {
            return null;
        }
        return recoveryGuideRepository.findByTreatmentOrderByDDayMinAsc(treatment).stream()
                .filter(g -> dDay >= g.getDDayMin() && dDay <= g.getDDayMax())
                .findFirst()
                .orElse(null);
    }

    private RuleVerdict evaluateRuleBasedConsultation(List<SymptomComparison> comparisons) {
        return comparisons.stream()
                .filter(c -> c.previousIntensity() == 3 && c.currentIntensity() == 3)
                .findFirst()
                .map(c -> new RuleVerdict(true, c.tagName()))
                .orElse(new RuleVerdict(false, null));
    }

    private String resolveConsultationCriteria(RuleVerdict ruleVerdict, AiGeneratedContent generated) {
        if (ruleVerdict.triggered()) {
            return "판정 기준: " + ruleVerdict.triggeredTagName() + " 강도 3(심함)이 2회 연속 기록";
        }
        if (generated.aiRiskDetected()) {
            return "AI가 기록 내용에서 주의가 필요한 표현을 감지했어요"
                    + (generated.aiRiskReason() != null ? " (" + generated.aiRiskReason() + ")" : "");
        }
        return null;
    }

    private AiFeedbackResponse.ComparisonInfo buildComparisonInfo(CareRecord previous, CareRecord current,
                                                                  List<SymptomComparison> comparisons) {
        List<AiFeedbackResponse.SymptomTrend> symptoms = comparisons.stream()
                .map(c -> new AiFeedbackResponse.SymptomTrend(
                        c.type(), c.tagName(), resolveTrend(c.previousIntensity(), c.currentIntensity()),
                        INTENSITY_LABEL.get(c.previousIntensity()), INTENSITY_LABEL.get(c.currentIntensity())))
                .toList();

        return new AiFeedbackResponse.ComparisonInfo(
                previous != null ? previous.getRecordId() : null,
                previous != null ? previous.getDDay() : null,
                previous != null ? firstPhotoUrl(previous) : null,
                firstPhotoUrl(current),
                symptoms,
                current.getStatusDescription()
        );
    }

    private String resolveTrend(int previousIntensity, int currentIntensity) {
        if (currentIntensity < previousIntensity) {
            return "down";
        }
        if (currentIntensity > previousIntensity) {
            return "up";
        }
        return "same";
    }

    private String buildPrompt(String treatmentName, int dDay, List<SymptomComparison> comparisons,
                               String statusDescription, double checklistRate, int photoCount,
                               RecoveryGuide guide, RuleVerdict ruleVerdict) {
        StringBuilder sb = new StringBuilder();
        sb.append("시술명: ").append(treatmentName).append("\n");
        sb.append("경과: D+").append(dDay).append("\n\n");

        if (comparisons.isEmpty()) {
            sb.append("이전 기록 없음 (첫 기록)\n\n");
        } else {
            sb.append("이전 기록 대비 증상 변화:\n");
            for (SymptomComparison c : comparisons) {
                sb.append("- ").append(c.tagName()).append(": ")
                        .append(INTENSITY_LABEL.get(c.previousIntensity())).append(" -> ")
                        .append(INTENSITY_LABEL.get(c.currentIntensity())).append("\n");
            }
            sb.append("\n");
        }

        sb.append("사용자 메모: ").append(statusDescription != null ? statusDescription : "없음").append("\n");
        sb.append("체크리스트 이행률: ").append(Math.round(checklistRate * 100)).append("%\n");
        sb.append("첨부 사진 수: ").append(photoCount).append("장\n\n");

        if (guide != null) {
            sb.append("이 시술의 D+").append(dDay).append(" 시점 일반적인 회복 가이드: ")
                    .append(guide.getCareGuidance()).append("\n\n");
        }

        if (ruleVerdict.triggered()) {
            sb.append("참고: ").append(ruleVerdict.triggeredTagName())
                    .append(" 강도가 2회 연속 심함으로 기록되어 시스템이 이미 병원 문의를 권고하기로 결정했습니다. ")
                    .append("consultationMessage에는 이 사실을 반영해서 문의를 권하는 문구를 작성하세요.\n\n");
        } else {
            sb.append("참고: 아직 병원 문의가 필요한 규칙 조건은 아닙니다. 문제 없으면 안심시키는 문구를 작성하세요.\n\n");
        }

        sb.append("위 정보를 참고해서 지정된 JSON 형식으로만 응답하세요.");
        return sb.toString();
    }

    private AiGeneratedContent callOpenAi(String userPrompt) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(20_000);
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "max_tokens", 500,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String response;
        try {
            response = restTemplate.postForObject(OPENAI_URL, request, String.class);
        } catch (ResourceAccessException e) {
            log.error("OpenAI 호출 타임아웃", e);
            throw new BaseException(ErrorStatus.GATEWAY_TIMEOUT_AI_ANALYSIS);
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(content);

            List<String> todayCare = new ArrayList<>();
            parsed.path("todayCare").forEach(node -> todayCare.add(node.asText()));

            return new AiGeneratedContent(
                    parsed.path("analysisSummary").asText(),
                    parsed.path("intensityReview").asText(),
                    todayCare,
                    parsed.path("consultationMessage").asText(),
                    parsed.path("aiRiskDetected").asBoolean(false),
                    parsed.hasNonNull("aiRiskReason") ? parsed.path("aiRiskReason").asText() : null
            );
        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패", e);
            throw new BaseException(ErrorStatus.GATEWAY_TIMEOUT_AI_ANALYSIS);
        }
    }

    private record SymptomComparison(String tagName, String type, int previousIntensity, int currentIntensity) {
    }

    private record RuleVerdict(boolean triggered, String triggeredTagName) {
    }

    private record AiGeneratedContent(String analysisSummary, String intensityReview, List<String> todayCare,
                                      String consultationMessage, boolean aiRiskDetected, String aiRiskReason) {
    }
}