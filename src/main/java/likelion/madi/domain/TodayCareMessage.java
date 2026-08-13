package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TODAY_CARE_MESSAGE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"card_id", "target_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayCareMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CareCard careCard;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Lob
    @Column(name = "generated_text", columnDefinition = "TEXT")
    private String generatedText;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TodayCareMessage(CareCard careCard, LocalDate targetDate, String generatedText) {
        this.careCard = careCard;
        this.targetDate = targetDate;
        this.generatedText = generatedText;
        this.createdAt = LocalDateTime.now();
    }
}