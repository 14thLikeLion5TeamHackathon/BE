package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.StatusTag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusTagResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tagId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Builder
    public StatusTagResponse(Long tagId, String name, String code) {
        this.tagId = tagId;
        this.name = name;
        this.code = code;
    }

    public static StatusTagResponse from(StatusTag statusTag) {
        return StatusTagResponse.builder()
                .tagId(statusTag.getTagId())
                .name(statusTag.getName())
                .code(statusTag.getCode())
                .build();
    }
}