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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "카카오 알림 메시지 발송")
    @PostMapping("/kakao/send")
    public ResponseEntity<ApiResponse<Void>> sendKakaoMessage(
            @AuthenticationPrincipal Long userId,
            @RequestParam("city") String city,
            @RequestParam("district") String district
    ) {
        notificationService.sendKakaoMessage(userId, city, district);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.KAKAO_MESSAGE_SEND_SUCCESS.getStatusCode(),
                        SuccessStatus.KAKAO_MESSAGE_SEND_SUCCESS.getMessage(),
                        null
                )
        );
    }
}