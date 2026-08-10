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
    private String storeName;
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
                .storeName(treatment.getStoreName())
                .storeLocation(treatment.getStoreLocation())
                .build();
    }
}