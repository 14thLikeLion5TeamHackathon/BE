package likelion.madi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoNotificationConnectRequest {

    @NotBlank(message = "카카오 인증 코드는 필수입니다.")
    private String authCode;

    @NotBlank(message = "리다이렉트 URI는 필수입니다.")
    private String redirectUri;
}