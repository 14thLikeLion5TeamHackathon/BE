package likelion.madi.service;

import likelion.madi.domain.KakaoNotification;
import likelion.madi.repository.KakaoNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoMessageScheduler {

    private final KakaoNotificationRepository kakaoNotificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9,12,15 * * *")
    public void sendDailyKakaoMessages() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();
        for (KakaoNotification notification : notifications) {
            Long userId = notification.getUser().getUserId();
            notificationService.sendKakaoMessage(userId, "서울", "강남구");
        }

    }
}
