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

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "CARE_CHECKLIST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CareCard careCard;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Builder
    public CareChecklist(CareCard careCard, LocalDate checkDate, String label) {
        this.careCard = careCard;
        this.checkDate = checkDate;
        this.label = label;
        this.isChecked = false;
    }

    public void check() {
        this.isChecked = true;
        this.checkedAt = LocalDateTime.now();
    }

    public void uncheck() {
        this.isChecked = false;
        this.checkedAt = null;
    }
}
