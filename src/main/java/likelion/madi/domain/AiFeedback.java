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
    @Column(name = "change_summary")
    private String changeSummary;

    @Lob
    @Column(name = "care_guidance")
    private String careGuidance;

    @Column(name = "needs_consultation")
    private Boolean needsConsultation;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AiFeedback(CareRecord careRecord, String changeSummary, String careGuidance, Boolean needsConsultation) {
        this.careRecord = careRecord;
        this.changeSummary = changeSummary;
        this.careGuidance = careGuidance;
        this.needsConsultation = needsConsultation;
        this.createdAt = LocalDateTime.now();
    }
}
