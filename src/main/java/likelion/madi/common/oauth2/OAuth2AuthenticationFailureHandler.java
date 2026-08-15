package likelion.madi.common.oauth2;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Value("${app.oauth2.failure-redirect-uri:http://localhost:3000/login?error=oauth2_failure}")
    private String failureRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("OAuth2 인증 실패: {}", exception.getMessage(), exception);

        // 구체적인 오류 정보를 로그에 기록
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request Query String: {}", request.getQueryString());

        // 실패 원인별 실제 에러 코드를 뽑아서 전달 (없으면 일반 실패로 표시)
        String errorCode = (exception instanceof OAuth2AuthenticationException oauth2Exception
                && oauth2Exception.getError() != null)
                ? oauth2Exception.getError().getErrorCode()
                : "oauth2_failure";

        String redirectUrl = UriComponentsBuilder.fromUriString(failureRedirectUri)
                .replaceQueryParam("error", errorCode)
                .replaceQueryParam("message", exception.getMessage())
                .build()
                .encode()
                .toUriString();

        log.info("OAuth2 실패 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
