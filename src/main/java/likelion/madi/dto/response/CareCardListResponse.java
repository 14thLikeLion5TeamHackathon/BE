package likelion.madi.dto.response;

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
    private Long cardId;
    private String treatmentName;
    private LocalDate treatmentDate;
    private String status;
    private Integer recoveryTotalDays;

    private Long recordId;
    private LocalDate recordedAt;
    private Integer dDay;
    private List<String> photoUrls;
    private String statusDescription;
    private Integer redness;
    private Integer swelling;
    private Integer pain;
    private Integer dryness;
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