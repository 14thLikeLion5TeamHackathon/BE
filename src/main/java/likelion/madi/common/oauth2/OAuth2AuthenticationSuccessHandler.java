package likelion.madi.common.oauth2;

import java.io.IOException;

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

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000}")
    private String redirectUri;

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

        log.info("OAuth2 인증 성공, 토큰 발급 완료 - userId: {}", user.getUserId());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refresh", refreshToken)
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
