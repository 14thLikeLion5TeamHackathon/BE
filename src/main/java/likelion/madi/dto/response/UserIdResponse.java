package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserIdResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}