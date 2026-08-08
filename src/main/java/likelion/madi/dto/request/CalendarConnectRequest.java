package likelion.madi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "구글 캘린더 연동 요청 DTO")
public class CalendarConnectRequest {

    @Schema(description = "프론트엔드에서 발급받은 구글 인증 코드", example = "4/0AeaYSHC...")
    @NotBlank(message = "구글 인증 코드는 필수입니다.")
    private String authCode; // 프론트가 보내는 JSON의 auth_code 키값과 자동으로 매핑됩니다.

}