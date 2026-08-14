package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.UserUpdateRequest;
import likelion.madi.dto.response.UserIdResponse;
import likelion.madi.dto.response.UserInfoResponse;
import likelion.madi.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "마이페이지")
@RequestMapping("/api/v1/mypage")
public class MypageController {

    private final MypageService mypageService;

    @Operation(summary = "개인정보 조회")
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserInfoResponse result = mypageService.getUserInfo(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.USER_INFO_GET_SUCCESS.getStatusCode(),
                        SuccessStatus.USER_INFO_GET_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "개인정보 수정")
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserIdResponse>> updateUserInfo(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        UserIdResponse result = mypageService.updateUserInfo(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.USER_INFO_UPDATE_SUCCESS.getStatusCode(),
                        SuccessStatus.USER_INFO_UPDATE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "카카오 알림 연동 해제")
    @DeleteMapping("/notification/kakao")
    public ResponseEntity<ApiResponse<Void>> disconnectKakaoNotification(
            @AuthenticationPrincipal Long userId
    ) {
        mypageService.disconnectKakaoNotification(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.KAKAO_NOTIFICATION_DISCONNECT_SUCCESS.getStatusCode(),
                        SuccessStatus.KAKAO_NOTIFICATION_DISCONNECT_SUCCESS.getMessage(),
                        null
                )
        );
    }

        @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId
    ) {
        mypageService.withdraw(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.MEMBER_WITHDRAW_SUCCESS.getStatusCode(),
                        SuccessStatus.MEMBER_WITHDRAW_SUCCESS.getMessage(),
                        null
                )
        );
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String accessToken = authorizationHeader.substring("Bearer ".length());
        mypageService.logout(userId, accessToken);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.LOGOUT_SUCCESS.getStatusCode(),
                        SuccessStatus.LOGOUT_SUCCESS.getMessage(),
                        null
                )
        );
    }
}

