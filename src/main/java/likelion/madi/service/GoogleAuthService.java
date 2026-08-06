package likelion.madi.service;

import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.jwt.JwtService;
import likelion.madi.domain.SocialAccount;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleLoginResponse;
import likelion.madi.enums.SocialProvider;
import likelion.madi.repository.SocialAccountRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final JwtService jwtService;
    private final SocialAccountRepository socialAccountRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public GoogleLoginResponse loginWithGoogle(String googleAccessToken) {
        // 1. 구글 OAuth2 API 통신으로 유저 정보(sub, email) 조회
        GoogleUserInfo googleUserInfo = fetchGoogleUserInfo(googleAccessToken);
        String providerUserId = googleUserInfo.getSub();
        String googleEmail = googleUserInfo.getEmail();

        // 2. DB(SocialAccount) 조회 (SocialProvider.GOOGLE 사용)
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.GOOGLE, providerUserId);

        if (socialAccountOpt.isPresent()) {
            // [기존 회원]: 토큰 발급 및 DB 리프레시 토큰 갱신
            SocialAccount socialAccount = socialAccountOpt.get();
            User user = socialAccount.getUser();

            String accessToken = jwtService.createAccessToken(user);

            Long userId = user.getUserId();
            Long socialAccountId = socialAccount.getSocialAccountId();

            String refreshToken = jwtService.createRefreshToken(userId);

            user.updateRefreshToken(refreshToken);

            return GoogleLoginResponse.ofExistingUser(
                    userId,
                    socialAccountId,
                    providerUserId,
                    accessToken,
                    refreshToken
            );
        } else {
            // [신규 회원]: 추가 가입 유도 응답 (is_new_user = true)
            return GoogleLoginResponse.ofNewUser(providerUserId, googleEmail);
        }
    }

    private GoogleUserInfo fetchGoogleUserInfo(String googleAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    requestEntity,
                    GoogleUserInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("구글 유저 정보 조회 실패: {}", e.getMessage());
            throw new BadRequestException("유효하지 않은 구글 액세스 토큰입니다.");
        }
    }

    @Getter
    private static class GoogleUserInfo {
        private String sub;     // 구글 고유 유저 ID
        private String email;   // 구글 이메일
    }
}