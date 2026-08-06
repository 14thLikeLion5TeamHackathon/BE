package likelion.madi.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "구글 로그인 요청 DTO")
public class GoogleLoginRequest {

    @Schema(description = "구글 액세스 토큰", example = "ya29.a0AfH6SMB...")
    @NotBlank(message = "구글 액세스 토큰은 필수 입력 값입니다.")
    private String googleAccessToken;
}