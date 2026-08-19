package likelion.madi.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.AiFeedback;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFeedbackResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long feedbackId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recordId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String treatmentName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dDay;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "이전 기록이 없으면 null")
    private ComparisonInfo comparison;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String analysisSummary;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "분석 태그가 없으면 null")
    private AnalysisTags analysisTags;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String intensityReview;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> todayCare;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean needsConsultation;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String consultationMessage;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String consultationCriteria;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    @Builder
    public AiFeedbackResponse(Long feedbackId, Long recordId, Long cardId, String treatmentName, Integer dDay,
                              ComparisonInfo comparison, String analysisSummary, AnalysisTags analysisTags,
                              String intensityReview, List<String> todayCare, Boolean needsConsultation,
                              String consultationMessage, String consultationCriteria, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.recordId = recordId;
        this.cardId = cardId;
        this.treatmentName = treatmentName;
        this.dDay = dDay;
        this.comparison = comparison;
        this.analysisSummary = analysisSummary;
        this.analysisTags = analysisTags;
        this.intensityReview = intensityReview;
        this.todayCare = todayCare;
        this.needsConsultation = needsConsultation;
        this.consultationMessage = consultationMessage;
        this.consultationCriteria = consultationCriteria;
        this.createdAt = createdAt;
    }

    public static AiFeedbackResponse from(AiFeedback feedback, Long cardId, String treatmentName,
                                          ObjectMapper objectMapper) {
        try {
            ComparisonInfo comparisonInfo = feedback.getComparison() != null
                    ? objectMapper.readValue(feedback.getComparison(), ComparisonInfo.class)
                    : null;
            AnalysisTags tags = feedback.getAnalysisTags() != null
                    ? objectMapper.readValue(feedback.getAnalysisTags(), AnalysisTags.class)
                    : null;
            List<String> todayCareList = feedback.getTodayCare() != null
                    ? objectMapper.readValue(feedback.getTodayCare(), new TypeReference<List<String>>() {
            })
                    : List.of();

            return AiFeedbackResponse.builder()
                    .feedbackId(feedback.getFeedbackId())
                    .recordId(feedback.getCareRecord().getRecordId())
                    .cardId(cardId)
                    .treatmentName(treatmentName)
                    .dDay(feedback.getCareRecord().getDDay())
                    .comparison(comparisonInfo)
                    .analysisSummary(feedback.getChangeSummary())
                    .analysisTags(tags)
                    .intensityReview(feedback.getIntensityReview())
                    .todayCare(todayCareList)
                    .needsConsultation(feedback.getNeedsConsultation())
                    .consultationMessage(feedback.getConsultationMessage())
                    .consultationCriteria(feedback.getConsultationCriteria())
                    .createdAt(feedback.getCreatedAt())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("AI 피드백 데이터 변환에 실패했습니다.", e);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonInfo {
        private Long previousRecordId;
        private Integer previousDDay;
        private String previousPhotoUrl;
        private String currentPhotoUrl;
        private List<SymptomTrend> symptoms;
        private String userComment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymptomTrend {
        private String type;
        private String name;
        private String trend;
        private String previousLabel;
        private String currentLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisTags {
        private String treatmentDay;
        private Double checklistRate;
        private Integer photoCount;
    }
}