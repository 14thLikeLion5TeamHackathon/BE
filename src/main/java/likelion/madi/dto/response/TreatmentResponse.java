package likelion.madi.dto.response;

import likelion.madi.domain.Treatment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentResponse {
    private Long treatmentId;
    private String name;
    private String category;
    private String description;

    @Builder
    public TreatmentResponse(Long treatmentId, String name, String category, String description) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.category = category;
    }

    public static TreatmentResponse from(Treatment treatment) {
        return TreatmentResponse.builder()
                .treatmentId(treatment.getTreatmentId())
                .name(treatment.getName())
                .category(treatment.getCategory())
                .description(treatment.getDescription())
                .build();
    }
}
