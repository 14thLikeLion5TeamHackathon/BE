package likelion.madi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.KakaoNotification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KakaoNotificationResponse {
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "카카오 알림 연동 정보가 없으면 null")
    private Long notificationId;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "카카오 알림 연동 정보가 없으면 null")
    private Long userId;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "카카오 알림 연동 정보가 없으면 null")
    private Boolean consent;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "카카오 알림 연동 정보가 없으면 null")
    private LocalDateTime consentedAt;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean connected;

    public static KakaoNotificationResponse from(KakaoNotification notification) {
        return KakaoNotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .consent(notification.getConsent())
                .consentedAt(notification.getConsentedAt())
                .connected(true)
                .build();
    }
    public static KakaoNotificationResponse notConnected() {
        return KakaoNotificationResponse.builder()
                .connected(false)
                .build();
    }

}
