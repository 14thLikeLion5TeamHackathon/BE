package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleTokenResponse {

    @JsonProperty("access_token")  // 👈 구글이 보낸 access_token을 읽어오는 필수 어노테이션
    private String accessToken;

    @JsonProperty("refresh_token") // 👈 구글이 보낸 refresh_token을 읽어오는 필수 어노테이션
    private String refreshToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;
}