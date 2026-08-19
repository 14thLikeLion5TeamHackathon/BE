package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate treatmentDate;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
