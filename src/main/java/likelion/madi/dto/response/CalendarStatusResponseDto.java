package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Long connectionId;
    private String googleEmail;
    private ConnectionStatus status; // 또는 String (CONNECTED, DISCONNECTED)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime connectedAt;
}