package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 오늘의 체크리스트를 마지막으로 생성했을 당시의 조건을 저장. 새로고침 시 지금 상태와 비교해서
// 실제로 바뀐 게 있을 때만 재생성하기 위한 용도(BriefingCache와 동일한 방식: 매번 새 행으로 적재).
@Entity
@Table(name = "CHECKLIST_GENERATION_CONTEXT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistGenerationContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate targetDate;
    private String city;
    private String district;
    private LocalDateTime latestRecordAt;

    @Column(columnDefinition = "TEXT")
    private String cardIds;

    @Column(columnDefinition = "TEXT")
    private String scheduleSignature;

    @Builder
    public ChecklistGenerationContext(User user, LocalDate targetDate, String city, String district,
                                      LocalDateTime latestRecordAt, String cardIds, String scheduleSignature) {
        this.user = user;
        this.targetDate = targetDate;
        this.city = city;
        this.district = district;
        this.latestRecordAt = latestRecordAt;
        this.cardIds = cardIds;
        this.scheduleSignature = scheduleSignature;
    }

    // 지금 상태랑 이 컨텍스트가 생성될 당시 조건이 동일한지 (동일하면 재생성 불필요)
    public boolean matches(String city, String district, LocalDateTime latestRecordAt,
                           String cardIds, String scheduleSignature) {
        return java.util.Objects.equals(this.city, city)
                && java.util.Objects.equals(this.district, district)
                && java.util.Objects.equals(this.latestRecordAt, latestRecordAt)
                && java.util.Objects.equals(this.cardIds, cardIds)
                && java.util.Objects.equals(this.scheduleSignature, scheduleSignature);
    }
}
