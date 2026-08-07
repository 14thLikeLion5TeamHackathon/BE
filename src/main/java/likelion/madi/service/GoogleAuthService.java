package likelion.madi.service;

import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.InternalServerException;
import likelion.madi.common.exception.UnauthorizedException;
import likelion.madi.common.jwt.JwtService;
import likelion.madi.domain.SocialAccount;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleLoginResponse;
import likelion.madi.enums.SocialProvider;
import likelion.madi.repository.SocialAccountRepository;
import likelion.madi.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public GoogleLoginResponse loginWithGoogle(String googleAccessToken) {
        // ❌ Case 1: 요청 값 누락 (400 Bad Request)
        if (googleAccessToken == null || googleAccessToken.trim().isEmpty()) {
            throw new BadRequestException("구글 인증 토큰이 누락되었습니다.");
        }

        // 구글 OAuth2 UserInfo 조회
        GoogleUserInfo googleUserInfo = fetchGoogleUserInfo(googleAccessToken);
        String providerUserId = googleUserInfo.getSub();
        String googleEmail = googleUserInfo.getEmail();
        String googleName = googleUserInfo.getName();

        // DB 소셜 계정 조회
        Optional<SocialAccount> socialAccountOpt = socialAccountRepository
                .findByProviderAndProviderUserId(SocialProvider.GOOGLE, providerUserId);

        User user;
        SocialAccount socialAccount;
        boolean isNewUser = false;

        if (socialAccountOpt.isPresent()) {
            // [Case 1: 기존 회원]
            socialAccount = socialAccountOpt.get();
            user = socialAccount.getUser();
        } else {
            // [Case 2: 신규 회원] -> 즉시 DB 자동 가입 처리
            isNewUser = true;

            user = User.builder()

                    .name(googleName)
                    .build();
            userRepository.save(user);

            socialAccount = SocialAccount.builder()
                    .user(user)
                    .provider(SocialProvider.GOOGLE)
                    .providerUserId(providerUserId)
                    .build();
            socialAccountRepository.save(socialAccount);
        }

        // 서비스 전용 Access / Refresh Token 발급 
        String accessToken = jwtService.createAccessToken(user);
        Long userId = user.getUserId();
        Long socialAccountId = socialAccount.getSocialAccountId();
        String refreshToken = jwtService.createRefreshToken(userId);

        // 리프레시 토큰 DB 갱신
        user.updateRefreshToken(refreshToken);

        return GoogleLoginResponse.builder()
                .isNewUser(isNewUser)
                .userId(userId)
                .socialAccountId(socialAccountId)
                .provider("google")
                .providerUserId(providerUserId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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

        } catch (HttpClientErrorException e) {
            // ❌ Case 2: 구글 토큰 만료/유효하지 않음 (401 Unauthorized)
            log.error("구글 토큰 검증 실패 [Status: {}]: {}", e.getStatusCode(), e.getMessage());
            throw new UnauthorizedException("유효하지 않거나 만료된 구글 인증 토큰입니다.");

        } catch (Exception e) {
            // ❌ Case 3: 구글 서버 장애 및 네트워크 오류 (500 Internal Server Error)
            log.error("구글 인증 서버 통신 중 오류 발생: {}", e.getMessage());
            throw new InternalServerException("구글 인증 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Getter
    private static class GoogleUserInfo {
        private String sub;     // 구글 고유 유저 ID
        private String email;   // 구글 이메일
        private String name;    // 구글 이름
    }
}