package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BRIEFING_CACHE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingCache {

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
    private String actionSentence;

    private String cautionLevel;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    @Builder
    public BriefingCache(User user, LocalDate targetDate, String city, String district, LocalDateTime latestRecordAt,
                         String cardIds, String actionSentence, String cautionLevel, String reasons) {
        this.user = user;
        this.targetDate = targetDate;
        this.city = city;
        this.district = district;
        this.latestRecordAt = latestRecordAt;
        this.cardIds = cardIds;
        this.actionSentence = actionSentence;
        this.cautionLevel = cautionLevel;
        this.reasons = reasons;
    }
}