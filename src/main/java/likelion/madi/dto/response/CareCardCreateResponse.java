package likelion.madi.dto.response;

import likelion.madi.domain.CareCard;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareCardCreateResponse {
    private Long cardId;
    private LocalDate treatmentDate;
    private LocalDateTime createdAt;

    @Builder
    public CareCardCreateResponse(Long cardId, LocalDate treatmentDate, LocalDateTime createdAt) {
        this.cardId = cardId;
        this.treatmentDate = treatmentDate;
        this.createdAt = createdAt;
    }

    public static CareCardCreateResponse from(CareCard careCard) {
        return CareCardCreateResponse.builder()
                .cardId(careCard.getCardId())
                .treatmentDate(careCard.getTreatmentDate())
                .createdAt(careCard.getCreatedAt())
                .build();
    }

}
