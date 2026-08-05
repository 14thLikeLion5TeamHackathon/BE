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

// 카카오 로그인 계정으로 메시지를 발송하므로 별도 연동 절차(전화번호 입력, 토큰 발급)가 없다.
// 이 테이블은 "카카오톡 메시지 수신에 동의했는지"만 기록한다.
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

    @Builder
    public KakaoNotification(User user, Boolean consent) {
        this.user = user;
        this.consent = consent;
        this.consentedAt = LocalDateTime.now();
    }

    public void updateConsent(Boolean consent) {
        this.consent = consent;
        this.consentedAt = LocalDateTime.now();
    }
}
