package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import likelion.madi.enums.CautionLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CARE_JUDGEMENT", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"card_id", "judged_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareJudgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "judgement_id")
    private Long judgementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CareCard careCard;

    @Column(name = "judged_date")
    private LocalDate judgedDate;

    @Lob
    @Column(name = "action_sentence", columnDefinition = "TEXT")
    private String actionSentence;

    @Enumerated(EnumType.STRING)
    @Column(name = "caution_level", length = 20)
    private CautionLevel cautionLevel;

    @Lob
    @Column(name = "reasons", columnDefinition = "TEXT")
    private String reasons;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CareJudgement(CareCard careCard, LocalDate judgedDate, String actionSentence,
                          CautionLevel cautionLevel, String reasons) {
        this.careCard = careCard;
        this.judgedDate = judgedDate;
        this.actionSentence = actionSentence;
        this.cautionLevel = cautionLevel;
        this.reasons = reasons;
        this.createdAt = LocalDateTime.now();
    }

    public void refresh(String actionSentence, CautionLevel cautionLevel, String reasons) {
        this.actionSentence = actionSentence;
        this.cautionLevel = cautionLevel;
        this.reasons = reasons;
    }
}
