package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_FEEDBACK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Lob
    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @Lob
    @Column(name = "care_guidance", columnDefinition = "TEXT")
    private String careGuidance;

    @Lob
    @Column(name = "comparison", columnDefinition = "TEXT")
    private String comparison;

    @Lob
    @Column(name = "analysis_tags", columnDefinition = "TEXT")
    private String analysisTags;

    @Lob
    @Column(name = "intensity_review", columnDefinition = "TEXT")
    private String intensityReview;

    @Lob
    @Column(name = "today_care", columnDefinition = "TEXT")
    private String todayCare;

    @Column(name = "needs_consultation")
    private Boolean needsConsultation;

    @Lob
    @Column(name = "consultation_message", columnDefinition = "TEXT")
    private String consultationMessage;

    @Lob
    @Column(name = "consultation_criteria", columnDefinition = "TEXT")
    private String consultationCriteria;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Builder
    public AiFeedback(CareRecord careRecord, String changeSummary, String careGuidance, String comparison,
                      String analysisTags, String intensityReview, String todayCare, Boolean needsConsultation,
                      String consultationMessage, String consultationCriteria) {
        this.careRecord = careRecord;
        this.changeSummary = changeSummary;
        this.careGuidance = careGuidance;
        this.comparison = comparison;
        this.analysisTags = analysisTags;
        this.intensityReview = intensityReview;
        this.todayCare = todayCare;
        this.needsConsultation = needsConsultation;
        this.consultationMessage = consultationMessage;
        this.consultationCriteria = consultationCriteria;
        this.createdAt = LocalDateTime.now();
    }
}
