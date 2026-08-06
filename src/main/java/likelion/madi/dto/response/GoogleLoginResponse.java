package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "구글 로그인 응답 DTO")
public class GoogleLoginResponse {

    @Schema(description = "신규 회원 여부 (true: 추가 정보 입력 필요, false: 로그인 완료)", example = "false")
    private Boolean isNewUser;

    @Schema(description = "유저 ID (기존 회원일 경우)", example = "1")
    private Long userId;

    @Schema(description = "소셜 계정 ID (기존 회원일 경우)", example = "1")
    private Long socialAccountId;

    @Schema(description = "소셜 제공자", example = "google")
    private String provider;

    @Schema(description = "구글 고유 유저 ID", example = "10928374921839210")
    private String providerUserId;

    @Schema(description = "구글 이메일 (신규 회원일 경우)", example = "user@gmail.com")
    private String googleEmail;

    @Schema(description = "서비스 Access Token (기존 회원일 경우)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "서비스 Refresh Token (기존 회원일 경우)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    public static GoogleLoginResponse ofExistingUser(Long userId, Long socialAccountId, String providerUserId, String accessToken, String refreshToken) {
        return GoogleLoginResponse.builder()
                .isNewUser(false)
                .userId(userId)
                .socialAccountId(socialAccountId)
                .provider("google")
                .providerUserId(providerUserId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static GoogleLoginResponse ofNewUser(String providerUserId, String googleEmail) {
        return GoogleLoginResponse.builder()
                .isNewUser(true)
                .provider("google")
                .providerUserId(providerUserId)
                .googleEmail(googleEmail)
                .build();
    }
}