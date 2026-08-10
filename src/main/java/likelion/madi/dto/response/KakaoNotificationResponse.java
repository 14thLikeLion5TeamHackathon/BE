package likelion.madi.dto.response;

import likelion.madi.domain.KakaoNotification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KakaoNotificationResponse {
    private Long notificationId;
    private Long userId;
    private Boolean consent;
    private LocalDateTime consentedAt;

    public static KakaoNotificationResponse from(KakaoNotification notification) {
        return KakaoNotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                    .userId(notification.getUser().getUserId())
                    .consent(notification.getConsent())
                .consentedAt(notification.getConsentedAt())
                    .build();
    }
}
