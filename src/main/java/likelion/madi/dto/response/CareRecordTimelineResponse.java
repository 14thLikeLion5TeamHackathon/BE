package likelion.madi.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordTimelineResponse {
    private Long cardId;
    private List<CareRecordTimelineItem> careRecords;

    @Builder
    public CareRecordTimelineResponse(Long cardId, List<CareRecordTimelineItem> careRecords) {
        this.cardId = cardId;
        this.careRecords = careRecords;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CareRecordTimelineItem {
        private Long recordId;
        private LocalDate recordedAt;
        private Integer dDay;
        private String photoUrl;
        private String statusDescription;
        private Integer redness;
        private Integer swelling;
        private Integer pain;
        private Integer dryness;
        private AiFeedbackItem aiFeedback;

        @Builder
        public CareRecordTimelineItem(Long recordId, LocalDate recordedAt, Integer dDay, String photoUrl,
                                      String statusDescription, Integer redness, Integer swelling,
                                      Integer pain, Integer dryness, AiFeedbackItem aiFeedback) {
            this.recordId = recordId;
            this.recordedAt = recordedAt;
            this.dDay = dDay;
            this.photoUrl = photoUrl;
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
        private Long feedbackId;
        private String changeSummary;
        private String careGuidance;
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