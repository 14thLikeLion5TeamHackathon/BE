package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "CARE_CARD_TREATMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCardTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "care_card_treatment_id")
    private Long careCardTreatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CareCard careCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @Column(name = "custom_name", length = 100)
    private String customName;

    @Builder
    public CareCardTreatment(CareCard careCard, Treatment treatment, String customName) {
        this.careCard = careCard;
        this.treatment = treatment;
        this.customName = customName;
    }
}
