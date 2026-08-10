package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 카카오 로그인과 별개로 talk_message 동의를 받아 액세스/리프레시 토큰을 발급받아 저장한다.
// consent는 메시지 수신 on/off 상태, accessToken/refreshToken은 실제 발송에 사용된다.
@Entity
@Table(name = "KAKAO_NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "consent")
    private Boolean consent;

    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Builder
    public KakaoNotification(User user, Boolean consent, String accessToken, String refreshToken) {
        this.user = user;
        this.consent = consent;
        this.consentedAt = LocalDateTime.now();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void updateConsent(Boolean consent) {
        this.consent = consent;
        this.consentedAt = LocalDateTime.now();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.consent = true;
        this.consentedAt = LocalDateTime.now();
    }
}
