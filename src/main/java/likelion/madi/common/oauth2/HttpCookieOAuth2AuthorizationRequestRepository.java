package likelion.madi.common.oauth2;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 세션 대신 쿠키에 OAuth2 인증 요청(state 등)을 임시 저장.
// STATELESS 세션 정책과 세션 기반 저장소가 충돌해 콜백 시 "authorization_request_not_found"가
// 간헐적으로 나던 문제를 해결하기 위함 (세션 상태에 의존하지 않게 됨).
// 자바 네이티브 직렬화 대신 JSON으로 필요한 필드만 담아서 저장 (역직렬화 취약점 방지).
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_MAX_AGE_SECONDS = 180;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }
        addCookie(response, serialize(authorizationRequest), COOKIE_MAX_AGE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response) {
        addCookie(response, "", 0);
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            AuthorizationRequestPayload payload = new AuthorizationRequestPayload(
                    authorizationRequest.getAuthorizationUri(),
                    authorizationRequest.getClientId(),
                    authorizationRequest.getRedirectUri(),
                    authorizationRequest.getScopes(),
                    authorizationRequest.getState(),
                    authorizationRequest.getAdditionalParameters(),
                    authorizationRequest.getAttributes());
            String json = objectMapper.writeValueAsString(payload);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("OAuth2 인증 요청 직렬화에 실패했습니다.", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
            AuthorizationRequestPayload payload = objectMapper.readValue(json, AuthorizationRequestPayload.class);
            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(payload.authorizationUri())
                    .clientId(payload.clientId())
                    .redirectUri(payload.redirectUri())
                    .scopes(payload.scopes())
                    .state(payload.state())
                    .additionalParameters(payload.additionalParameters())
                    .attributes(payload.attributes())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private record AuthorizationRequestPayload(
            String authorizationUri,
            String clientId,
            String redirectUri,
            Set<String> scopes,
            String state,
            Map<String, Object> additionalParameters,
            Map<String, Object> attributes) {
    }
}
