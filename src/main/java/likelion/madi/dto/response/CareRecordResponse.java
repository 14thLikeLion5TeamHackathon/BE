package likelion.madi.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.CareRecord;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recordId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cardId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> photoUrls;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusDescription;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dDay;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime recordedAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Integer> tags;

    @Builder
    public CareRecordResponse(Long recordId, Long cardId, List<String> photoUrls, String statusDescription,
                              Integer dDay, LocalDateTime recordedAt, Map<String, Integer> tags) {
        this.recordId = recordId;
        this.cardId = cardId;
        this.photoUrls = photoUrls;
        this.statusDescription = statusDescription;
        this.dDay = dDay;
        this.recordedAt = recordedAt;
        this.tags = tags;
    }

    public static CareRecordResponse from(CareRecord careRecord) {
        Map<String, Integer> tagIntensityByCode = careRecord.getTags().stream()
                .collect(Collectors.toMap(t -> t.getStatusTag().getCode(), t -> t.getIntensity()));

        return CareRecordResponse.builder()
                .recordId(careRecord.getRecordId())
                .cardId(careRecord.getCareCard().getCardId())
                .photoUrls(careRecord.getPhotoUrls())
                .statusDescription(careRecord.getStatusDescription())
                .dDay(careRecord.getDDay())
                .recordedAt(careRecord.getRecordedAt())
                .tags(tagIntensityByCode)
                .build();
    }
}