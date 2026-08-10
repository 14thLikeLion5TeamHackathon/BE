package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import likelion.madi.dto.request.KakaoNotificationConnectRequest;
import likelion.madi.dto.request.KakaoNotificationRequest;
import likelion.madi.dto.response.KakaoNotificationResponse;
import likelion.madi.dto.response.KakaoTokenResponse;
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
    private final KakaoOAuthClient kakaoOAuthClient;

    @Transactional
    public KakaoNotificationResponse connectKakaoNotification(Long userId, KakaoNotificationConnectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getTokens(request.getAuthCode(), request.getRedirectUri());

        KakaoNotification notification = kakaoNotificationRepository.findByUser(user)
                .map(existing -> {
                    existing.updateTokens(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
                    return existing;
                })
                .orElseGet(() -> {
                    KakaoNotification created = KakaoNotification.builder()
                            .user(user)
                            .consent(true)
                            .accessToken(tokenResponse.getAccessToken())
                            .refreshToken(tokenResponse.getRefreshToken())
                            .build();
                    kakaoNotificationRepository.save(created);
                    return created;
                });

        return KakaoNotificationResponse.from(notification);
    }

    @Transactional
    public KakaoNotificationResponse updateConsent(Long userId, KakaoNotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        KakaoNotification notification = kakaoNotificationRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_KAKAO_NOTIFICATION));

        notification.updateConsent(request.getConsent());
        return KakaoNotificationResponse.from(notification);
    }
}
