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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// "오늘의 케어 체크리스트" 완료 상태 저장용. 카드 하나(=회복가이드의 오늘의 관리 문장 하나)당
// 하루에 한 건만 존재 (card_id + check_date 유니크). 완료율은 이 테이블을 집계해서 계산하고,
// AI 피드백의 "관리 이행" 판단에도 이 값을 사용한다.
@Entity
@Table(name = "CARE_CHECKLIST", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"card_id", "check_date"})
})
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

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Builder
    public CareChecklist(CareCard careCard, LocalDate checkDate) {
        this.careCard = careCard;
        this.checkDate = checkDate;
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
