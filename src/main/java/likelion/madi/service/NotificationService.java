package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.common.util.KstDate;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.request.KakaoNotificationConnectRequest;
import likelion.madi.dto.request.KakaoNotificationRequest;
import likelion.madi.dto.response.KakaoNotificationResponse;
import likelion.madi.dto.response.KakaoTokenResponse;
import likelion.madi.dto.response.TodayChecklistResponse;
import likelion.madi.dto.response.BriefingResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.springframework.core.NestedExceptionUtils.buildMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final KakaoNotificationRepository kakaoNotificationRepository;
    private final CareCardRepository careCardRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoMessageClient kakaoMessageClient;
    private final TodayChecklistService todayChecklistService;
    private final BriefingService briefingService;
    private final RiskWarningService riskWarningService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

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


    @Transactional(readOnly = true)
    public KakaoNotificationResponse getKakaoNotificationStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        return kakaoNotificationRepository.findByUser(user)
                .map(KakaoNotificationResponse::from)
                .orElseGet(KakaoNotificationResponse::notConnected);
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
    public void sendKakaoMessage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        KakaoNotification notification = kakaoNotificationRepository.findByUser(user)
                .filter(KakaoNotification::getConsent)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_KAKAO_NOTIFICATION));

        LocalDate today = KstDate.today();

        TodayChecklistResponse checklist = todayChecklistService.getTodayChecklist(userId, today);
        BriefingResponse briefing = briefingService.getBriefing(user, today, user.getCity(), user.getDistrict(), false);

        if (briefing.getCardJudgement() == null && checklist.getItems().isEmpty()) {
            return;
        }

        String message = buildMessage(checklist, briefing);

        log.error("[DEBUG] frontendBaseUrl 실제 값 = [{}]", frontendBaseUrl);

        try {
            kakaoMessageClient.sendMessage(notification.getAccessToken(), message, frontendBaseUrl, "오늘의 케어 보기");
        } catch (Exception e) {
            KakaoTokenResponse refreshed = kakaoOAuthClient.refreshAccessToken(notification.getRefreshToken());
            notification.updateTokens(refreshed.getAccessToken(), refreshed.getRefreshToken());
            kakaoMessageClient.sendMessage(notification.getAccessToken(), message, frontendBaseUrl, "오늘의 케어 보기");
        }
    }

    @Transactional
    public void sendRecordReminder() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();

        for (KakaoNotification notification : notifications) {
            try {
                User user = notification.getUser();
                List<CareCard> cards = careCardRepository.findByUser(user);

                for (CareCard card : cards) {
                    int dDay = (int) ChronoUnit.DAYS.between(card.getTreatmentDate(), KstDate.today());
                    if (dDay == 3 || dDay == 7) {
                        CareCardTreatment primary = card.getTreatments().get(0);
                        String treatmentName = primary.getTreatment() != null
                                ? primary.getTreatment().getName()
                                : primary.getCustomName();

                        String message = treatmentName + " 받은 지 " + dDay + "일차예요! 오늘 상태를 기록해보세요.";
                        String link = frontendBaseUrl + "/record/" + card.getCardId();

                        try {
                            kakaoMessageClient.sendMessage(notification.getAccessToken(), message, link);
                        } catch (Exception e) {
                            KakaoTokenResponse refreshed = kakaoOAuthClient.refreshAccessToken(notification.getRefreshToken());
                            notification.updateTokens(refreshed.getAccessToken(), refreshed.getRefreshToken());
                            kakaoMessageClient.sendMessage(notification.getAccessToken(), message, link);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("기록 리마인드 발송 실패 - userId: {}, error: {}", notification.getUser().getUserId(), e.getMessage());
            }
        }
    }

    private String buildMessage(TodayChecklistResponse checklist, BriefingResponse briefing) {
        StringBuilder sb = new StringBuilder();

        if (briefing.getCardJudgement() != null) {
            sb.append(briefing.getCardJudgement().getActionSentence());
        }

        if (!checklist.getItems().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append("오늘의 체크리스트:\n");
            for (TodayChecklistResponse.Item item : checklist.getItems()) {
                sb.append("- ").append(item.getLabel()).append("\n");
            }
        }

        return sb.toString().trim();
    }

    @Transactional
    public void sendRiskWarnings() {
        List<KakaoNotification> notifications = kakaoNotificationRepository.findByConsentTrue();

        for (KakaoNotification notification : notifications) {
            try {
                User user = notification.getUser();

                Optional<String> warning = riskWarningService.checkTomorrowRisk(user);

                if (warning.isPresent()) {
                    try {
                        kakaoMessageClient.sendMessage(
                                notification.getAccessToken(),
                                warning.get(),
                                frontendBaseUrl,
                                "일정 확인하기"
                        );
                    } catch (Exception e) {
                        KakaoTokenResponse refreshed = kakaoOAuthClient.refreshAccessToken(notification.getRefreshToken());
                        notification.updateTokens(refreshed.getAccessToken(), refreshed.getRefreshToken());
                        kakaoMessageClient.sendMessage(
                                notification.getAccessToken(),
                                warning.get(),
                                frontendBaseUrl,
                                "일정 확인하기"
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("위험 알림 발송 실패 - userId: {}, error: {}", notification.getUser().getUserId(), e.getMessage());
            }
        }
    }
}