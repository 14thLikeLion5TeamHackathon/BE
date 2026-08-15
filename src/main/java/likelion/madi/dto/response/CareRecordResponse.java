package likelion.madi.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import likelion.madi.domain.CareRecord;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordResponse {

    private Long recordId;
    private Long cardId;
    private String photoUrl;
    private String statusDescription;
    private Integer dDay;
    private LocalDateTime recordedAt;
    private Map<String, Integer> tags;

    @Builder
    public CareRecordResponse(Long recordId, Long cardId, String photoUrl, String statusDescription,
                              Integer dDay, LocalDateTime recordedAt, Map<String, Integer> tags) {
        this.recordId = recordId;
        this.cardId = cardId;
        this.photoUrl = photoUrl;
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
                .photoUrl(careRecord.getPhotoUrl())
                .statusDescription(careRecord.getStatusDescription())
                .dDay(careRecord.getDDay())
                .recordedAt(careRecord.getRecordedAt())
                .tags(tagIntensityByCode)
                .build();
    }
}