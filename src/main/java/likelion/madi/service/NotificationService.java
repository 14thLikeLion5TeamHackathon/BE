package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import likelion.madi.dto.request.KakaoNotificationRequest;
import likelion.madi.dto.response.KakaoNotificationResponse;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final KakaoNotificationRepository kakaoNotificationRepository;

    @Transactional
    public KakaoNotificationResponse connectKakaoNotification(Long userId, KakaoNotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        Optional<KakaoNotification> existing = kakaoNotificationRepository.findByUser(user);

        KakaoNotification notification;
        if (existing.isPresent()) {
            notification = existing.get();
            notification.updateConsent(request.getConsent());
        } else {
            notification = KakaoNotification.builder()
                    .user(user)
                    .consent(request.getConsent())
                    .build();
            kakaoNotificationRepository.save(notification);
        }

        return KakaoNotificationResponse.from(notification);
    }
}
