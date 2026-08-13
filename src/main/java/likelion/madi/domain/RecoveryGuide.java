package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RECOVERY_GUIDE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecoveryGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guide_id")
    private Long guideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @Column(name = "d_day_min")
    private Integer dDayMin;

    @Column(name = "d_day_max")
    private Integer dDayMax;

    @Lob
    @Column(name = "care_guidance", columnDefinition = "TEXT")
    private String careGuidance;

    @Lob
    @Column(name = "today_care", columnDefinition = "TEXT")
    private String todayCare;

    @Lob
    @Column(name = "caution", columnDefinition = "TEXT")
    private String caution;

    @Builder
    public RecoveryGuide(Treatment treatment, Integer dDayMin, Integer dDayMax,
                          String careGuidance, String todayCare, String caution) {
        this.treatment = treatment;
        this.dDayMin = dDayMin;
        this.dDayMax = dDayMax;
        this.careGuidance = careGuidance;
        this.todayCare = todayCare;
        this.caution = caution;
    }
}
