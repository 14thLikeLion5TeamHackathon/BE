package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.Treatment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long treatmentId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "연계된 매장이 없으면 null")
    private String storeName;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "연계된 매장이 없으면 null")
    private String storeLocation;

    @Builder
    public TreatmentResponse(Long treatmentId, String name, String category, String description,
                             String storeName, String storeLocation) {
        this.treatmentId = treatmentId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
    }

    public static TreatmentResponse from(Treatment treatment) {
        return TreatmentResponse.builder()
                .treatmentId(treatment.getTreatmentId())
                .name(treatment.getName())
                .category(treatment.getCategory())
                .description(treatment.getDescription())
                .storeName(treatment.getStore() != null ? treatment.getStore().getName() : null)
                .storeLocation(treatment.getStore() != null ? treatment.getStore().getAddress() : null)
                .build();
    }
}