package likelion.madi.controller;

import likelion.madi.dto.request.OnboardingRequest;
import likelion.madi.dto.response.OnboardingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.TokenRefreshRequest;
import likelion.madi.dto.response.TokenRefreshResponse;
import likelion.madi.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "토큰 갱신")
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenRefreshResponse result = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.TOKEN_REISSUE_SUCCESS.getStatusCode(),
                        SuccessStatus.TOKEN_REISSUE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "온보딩/회원정보등록")
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardingResponse>> onboard(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OnboardingRequest request
    ) {
        OnboardingResponse result = authService.onboard(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.ONBOARDING_SUCCESS.getStatusCode(),
                        SuccessStatus.ONBOARDING_SUCCESS.getMessage(),
                        result
                )
        );
    }
}