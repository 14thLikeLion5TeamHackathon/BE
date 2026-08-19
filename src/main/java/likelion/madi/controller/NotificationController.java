package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.KakaoNotificationConnectRequest;
import likelion.madi.dto.request.KakaoNotificationRequest;
import likelion.madi.dto.response.KakaoNotificationResponse;
import likelion.madi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "카카오톡 연동")
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "카카오 알림 연동 및 동의 설정")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoNotificationResponse>> connectKakaoNotification(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody KakaoNotificationConnectRequest request
    ) {
        KakaoNotificationResponse result = notificationService.connectKakaoNotification(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.KAKAO_NOTIFICATION_CONNECT_SUCCESS.getStatusCode(),
                        SuccessStatus.KAKAO_NOTIFICATION_CONNECT_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "카카오 알림 연동 상태 조회")
    @GetMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoNotificationResponse>> getKakaoNotificationStatus(
            @AuthenticationPrincipal Long userId
    ) {
        KakaoNotificationResponse result = notificationService.getKakaoNotificationStatus(userId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.KAKAO_NOTIFICATION_GET_SUCCESS.getStatusCode(),
                        SuccessStatus.KAKAO_NOTIFICATION_GET_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "카카오 알림 수신 on/off 전환")
    @PatchMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoNotificationResponse>> updateConsent(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody KakaoNotificationRequest request
    ) {
        KakaoNotificationResponse result = notificationService.updateConsent(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.KAKAO_NOTIFICATION_CONNECT_SUCCESS.getStatusCode(),
                        SuccessStatus.KAKAO_NOTIFICATION_CONNECT_SUCCESS.getMessage(),
                        result
                )
        );
    }
}