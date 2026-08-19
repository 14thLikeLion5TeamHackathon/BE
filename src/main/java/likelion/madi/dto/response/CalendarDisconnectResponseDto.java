package likelion.madi.dto.response; // 패키지는 프로젝트에 맞게 수정

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalendarDisconnectResponseDto {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int code;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "해제 성공 시 null")
    private Object data;
}