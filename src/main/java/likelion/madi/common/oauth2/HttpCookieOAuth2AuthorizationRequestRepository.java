package likelion.madi.common.oauth2;

import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// 세션 대신 쿠키에 OAuth2 인증 요청(state 등)을 임시 저장.
// STATELESS 세션 정책과 세션 기반 저장소가 충돌해 콜백 시 "authorization_request_not_found"가
// 간헐적으로 나던 문제를 해결하기 위함 (세션 상태에 의존하지 않게 됨).
// 자바 네이티브 직렬화 대신 JSON으로 필요한 필드만 담아서 저장 (역직렬화 취약점 방지).
//
// 로그인을 시작한 프론트 주소(redirect_uri)도 같이 쿠키에 담아뒀다가, 콜백이 끝나면 그 주소로
// 되돌려보낸다. 임의 주소로 오픈 리다이렉트가 나가지 않도록 화이트리스트(authorized-redirect-uris)와
// 대조해서 검증한 값만 저장/사용한다.
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_MAX_AGE_SECONDS = 180;

    public static final String REDIRECT_URI_PARAM_NAME = "redirect_uri";
    private static final String REDIRECT_URI_COOKIE_NAME = "redirect_uri";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.oauth2.authorized-redirect-uris}")
    private List<String> authorizedRedirectUris;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response, COOKIE_NAME);
            deleteCookie(response, REDIRECT_URI_COOKIE_NAME);
            return;
        }
        addCookie(response, COOKIE_NAME, serialize(authorizationRequest), COOKIE_MAX_AGE_SECONDS);

        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_NAME);
        if (redirectUri != null && isAuthorizedRedirectUri(redirectUri)) {
            addCookie(response, REDIRECT_URI_COOKIE_NAME, redirectUri, COOKIE_MAX_AGE_SECONDS);
        } else if (redirectUri != null) {
            log.warn("허용되지 않은 redirect_uri 요청을 무시했습니다: {}", redirectUri);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response, COOKIE_NAME);
        deleteCookie(response, REDIRECT_URI_COOKIE_NAME);
        return authorizationRequest;
    }

    // 로그인을 시작했던 프론트 주소를 반환한다 (화이트리스트에 있는 값만).
    public Optional<String> getRedirectUri(HttpServletRequest request) {
        return getCookie(request, REDIRECT_URI_COOKIE_NAME)
                .map(Cookie::getValue)
                .filter(this::isAuthorizedRedirectUri);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        try {
            URI target = URI.create(uri);
            return authorizedRedirectUris.stream().anyMatch(authorized -> {
                URI allowed = URI.create(authorized);
                return allowed.getScheme().equalsIgnoreCase(target.getScheme())
                        && allowed.getHost().equalsIgnoreCase(target.getHost())
                        && allowed.getPort() == target.getPort();
            });
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
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
