package likelion.madi.common.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilterExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    // 인증 실패 (ex. 토큰 없음/만료/위조)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        log.warn("인증 실패 - {}: {}", request.getRequestURI(), authException.getMessage());
        writeErrorResponse(response, ErrorStatus.UNAUTHORIZED_USER);
    }

    // 인가 실패 (ex. 권한 없는 리소스 접근)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        log.warn("인가 실패 - {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        writeErrorResponse(response, ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorStatus errorStatus) throws IOException {
        response.setStatus(errorStatus.getStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail(errorStatus.getStatusCode(), errorStatus.getMessage())));
    }
}
