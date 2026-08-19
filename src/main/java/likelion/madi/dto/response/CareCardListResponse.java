package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// 케어카드 리스트 조회 (전체 조회) 상세 페이지꺼
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCardListResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String treatmentName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate treatmentDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "매칭된 시술 정보가 없으면 null")
    private Integer recoveryTotalDays;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어 기록이 없으면 null")
    private Long recordId;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어 기록이 없으면 null")
    private LocalDate recordedAt;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어 기록이 없으면 null")
    private Integer dDay;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어 기록이 없으면 null")
    private List<String> photoUrls;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "케어 기록이 없으면 null")
    private String statusDescription;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "해당 태그 기록이 없으면 null")
    private Integer redness;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "해당 태그 기록이 없으면 null")
    private Integer swelling;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "해당 태그 기록이 없으면 null")
    private Integer pain;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "해당 태그 기록이 없으면 null")
    private Integer dryness;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "AI 피드백이 없으면 null")
    private AiFeedbackItem aiFeedback;

    @Builder
    public CareCardListResponse(Long cardId, String treatmentName, LocalDate treatmentDate, String status,
                                Integer recoveryTotalDays, Long recordId, LocalDate recordedAt, Integer dDay,
                                List<String> photoUrls, String statusDescription, Integer redness, Integer swelling,
                                Integer pain, Integer dryness, AiFeedbackItem aiFeedback) {
        this.cardId = cardId;
        this.treatmentName = treatmentName;
        this.treatmentDate = treatmentDate;
        this.status = status;
        this.recoveryTotalDays = recoveryTotalDays;
        this.recordId = recordId;
        this.recordedAt = recordedAt;
        this.dDay = dDay;
        this.photoUrls = photoUrls;
        this.statusDescription = statusDescription;
        this.redness = redness;
        this.swelling = swelling;
        this.pain = pain;
        this.dryness = dryness;
        this.aiFeedback = aiFeedback;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AiFeedbackItem {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long feedbackId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String changeSummary;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String careGuidance;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean needsConsultation;

        @Builder
        public AiFeedbackItem(Long feedbackId, String changeSummary, String careGuidance, Boolean needsConsultation) {
            this.feedbackId = feedbackId;
            this.changeSummary = changeSummary;
            this.careGuidance = careGuidance;
            this.needsConsultation = needsConsultation;
        }
    }
}