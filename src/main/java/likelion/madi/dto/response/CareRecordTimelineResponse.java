package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordTimelineResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CareRecordTimelineItem> careRecords;

    @Builder
    public CareRecordTimelineResponse(Long cardId, List<CareRecordTimelineItem> careRecords) {
        this.cardId = cardId;
        this.careRecords = careRecords;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CareRecordTimelineItem {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Long recordId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDate recordedAt;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer dDay;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<String> photoUrls;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
        public CareRecordTimelineItem(Long recordId, LocalDate recordedAt, Integer dDay, List<String> photoUrls,
                                      String statusDescription, Integer redness, Integer swelling,
                                      Integer pain, Integer dryness, AiFeedbackItem aiFeedback) {
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