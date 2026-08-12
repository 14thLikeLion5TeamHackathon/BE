package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.request.KakaoNotificationConnectRequest;
import likelion.madi.dto.request.KakaoNotificationRequest;
import likelion.madi.dto.response.KakaoNotificationResponse;
import likelion.madi.dto.response.KakaoTokenResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final KakaoNotificationRepository kakaoNotificationRepository;
    private final CareCardRepository careCardRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoMessageClient kakaoMessageClient;
    private final TodayCareService todayCareService;
    private final RiskWarningService riskWarningService;

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

    @Transactional
    public void sendKakaoMessage(Long userId, String city, String district) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        KakaoNotification notification = kakaoNotificationRepository.findByUser(user)
                .filter(KakaoNotification::getConsent)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_KAKAO_NOTIFICATION));

        CareCard activeCard = careCardRepository.findByUser(user).stream()
                .filter(this::isInProgress)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_CARD));

        CareCardTreatment primary = activeCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        int dDay = (int) ChronoUnit.DAYS.between(activeCard.getTreatmentDate(), LocalDate.now());

        String message = todayCareService.generateOrGetTodayCare(activeCard, treatment, dDay, city, district);

        kakaoMessageClient.sendMessage(notification.getAccessToken(), message, "http://localhost:3000", "오늘의 케어 보기");
    }

    @Transactional
    public void sendRecordReminder() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();

        for (KakaoNotification notification : notifications) {
            User user = notification.getUser();
            List<CareCard> cards = careCardRepository.findByUser(user);

            for (CareCard card : cards) {
                int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), LocalDate.now());
                if (dDay == 3 || dDay == 7) {
                    CareCardTreatment primary = card.getTreatments().get(0);
                    String treatmentName = primary.getTreatment() != null
                            ? primary.getTreatment().getName()
                            : primary.getCustomName();

                    String message = treatmentName + " 받은 지 " + dDay + "일차예요! 오늘 상태를 기록해보세요.";
                    String link = "http://localhost:3000/record/" + card.getCardId();

                    kakaoMessageClient.sendMessage(notification.getAccessToken(), message, link);
                }
            }
        }
    }

    // 진행중인 카드인지 판단 (회복 총 기간을 넘지 않았는지)
    private boolean isInProgress(CareCard card) {
        CareCardTreatment primary = card.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        if (treatment == null || treatment.getRecoveryTotalDays() == null) {
            return true;
        }
        int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), LocalDate.now());
        return dDay <= treatment.getRecoveryTotalDays();
    }

    @Transactional
    public void sendRiskWarnings() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();

        for (KakaoNotification notification : notifications) {
            User user = notification.getUser();

            Optional<String> warning = riskWarningService.checkTomorrowRisk(user);

            if (warning.isPresent()) {
                kakaoMessageClient.sendMessage(
                        notification.getAccessToken(),
                        warning.get(),
                        "http://localhost:3000",
                        "일정 확인하기"
                );
            }
        }
    }
}