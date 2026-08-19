package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.enums.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarStatusResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long connectionId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String googleEmail;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) // 또는 String (CONNECTED, DISCONNECTED)
    private ConnectionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime connectedAt;
}