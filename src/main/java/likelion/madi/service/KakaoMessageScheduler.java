package likelion.madi.service;

import likelion.madi.domain.KakaoNotification;
import likelion.madi.repository.KakaoNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMessageScheduler {

    private final KakaoNotificationRepository kakaoNotificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9,18 * * *", zone = "Asia/Seoul")
    public void sendDailyKakaoMessages() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();
        for (KakaoNotification notification : notifications) {
            Long userId = notification.getUser().getUserId();
            try {
                notificationService.sendKakaoMessage(userId);
            } catch (Exception e) {
                log.warn("카카오 메시지 발송 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        }
    }

    // ⚠️ 테스트용 임시 발송 (오늘 15:50 한 번). 테스트 끝나면 이 메서드 지워주세요.
    @Scheduled(cron = "0 50 15 * * *", zone = "Asia/Seoul")
    public void sendTestKakaoMessage() {
        sendDailyKakaoMessages();
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendDailyRecordReminders() {
        notificationService.sendRecordReminder();
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendDailyRiskWarnings() {
        notificationService.sendRiskWarnings();
    }
}
