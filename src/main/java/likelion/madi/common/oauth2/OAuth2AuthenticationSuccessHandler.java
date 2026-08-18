package likelion.madi.common.oauth2;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.madi.common.jwt.JwtService;
import likelion.madi.domain.User;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    // 로그인 시작 시 redirect_uri 파라미터가 없거나 화이트리스트 밖일 때 쓰는 기본값 (목록의 첫 번째 주소).
    @Value("${app.oauth2.authorized-redirect-uris}")
    private List<String> authorizedRedirectUris;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 authorities에 user_id를 넣어줬습니다.
        Long userId = oauth2User.getAuthorities().stream()
                .findFirst()
                .map(authority -> Long.valueOf(authority.getAuthority()))
                .orElseThrow(() -> new IllegalStateException("OAuth2User에 사용자 정보가 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user.getUserId());

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        log.info("OAuth2 인증 성공, 토큰 발급 완료 - userId: {}", user.getUserId());

        String redirectUri = authorizationRequestRepository.getRedirectUri(request)
                .orElseGet(() -> authorizedRedirectUris.get(0));

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refresh", refreshToken)
                .queryParam("isNewUser", !user.isOnboarded())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
