package likelion.madi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CARE_CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "treatment_date")
    private LocalDate treatmentDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "careCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareCardTreatment> treatments = new ArrayList<>();

    @Builder
    public CareCard(User user, LocalDate treatmentDate) {
        this.user = user;
        this.treatmentDate = treatmentDate;
        this.createdAt = LocalDateTime.now();
    }

    public void updateTreatmentDate(LocalDate treatmentDate) {
        this.treatmentDate = treatmentDate;
    }

    public void addTreatment(CareCardTreatment careCardTreatment) {
        this.treatments.add(careCardTreatment);
    }
}
