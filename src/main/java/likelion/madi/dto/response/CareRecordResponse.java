package likelion.madi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<TagInfo> tags;

    @Builder
    public CareRecordResponse(Long recordId, Long cardId, String photoUrl, String statusDescription,
                              Integer dDay, LocalDateTime recordedAt, List<TagInfo> tags) {
        this.recordId = recordId;
        this.cardId = cardId;
        this.photoUrl = photoUrl;
        this.statusDescription = statusDescription;
        this.dDay = dDay;
        this.recordedAt = recordedAt;
        this.tags = tags;
    }

    public static CareRecordResponse from(CareRecord careRecord) {
        List<TagInfo> tagInfos = careRecord.getTags().stream()
                .map(t -> new TagInfo(t.getStatusTag().getTagId(), t.getStatusTag().getName(),
                        t.getStatusTag().getCode(), t.getIntensity()))
                .toList();

        return CareRecordResponse.builder()
                .recordId(careRecord.getRecordId())
                .cardId(careRecord.getCareCard().getCardId())
                .photoUrl(careRecord.getPhotoUrl())
                .statusDescription(careRecord.getStatusDescription())
                .dDay(careRecord.getDDay())
                .recordedAt(careRecord.getRecordedAt())
                .tags(tagInfos)
                .build();
    }

    @Getter
    public static class TagInfo {
        private final Long tagId;
        private final String name;
        private final String code;
        private final Integer intensity;

        public TagInfo(Long tagId, String name, String code, Integer intensity) {
            this.tagId = tagId;
            this.name = name;
            this.code = code;
            this.intensity = intensity;
        }
    }
}