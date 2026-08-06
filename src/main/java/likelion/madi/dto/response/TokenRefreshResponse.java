package likelion.madi.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class) // 전역 SNAKE_CASE 설정 무시하고 이 DTO만 카멜케이스 유지
public class TokenRefreshResponse {

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    @Builder
    public TokenRefreshResponse(String grantType, String accessToken, String refreshToken, long expiresIn) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}