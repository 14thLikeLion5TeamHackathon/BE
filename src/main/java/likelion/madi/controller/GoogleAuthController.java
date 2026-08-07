package likelion.madi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.dto.request.GoogleLoginRequest;
import likelion.madi.dto.response.GoogleLoginResponse;
import likelion.madi.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @Operation(summary = "구글 로그인")
    @PostMapping("/login/google")
    public ResponseEntity<ApiResponse<GoogleLoginResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        GoogleLoginResponse result = googleAuthService.loginWithGoogle(request.getGoogleAccessToken());
        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }
}