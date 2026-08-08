package likelion.madi.dto.response;

import likelion.madi.domain.StatusTag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusTagResponse {

    private Long tagId;
    private String name;

    @Builder
    public StatusTagResponse(Long tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public static StatusTagResponse from(StatusTag statusTag) {
        return StatusTagResponse.builder()
                .tagId(statusTag.getTagId())
                .name(statusTag.getName())
                .build();
    }
}