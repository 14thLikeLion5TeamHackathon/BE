package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import likelion.madi.enums.ConnectionStatus;

import java.time.LocalDateTime;

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

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "consent")
    private Boolean consent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ConnectionStatus status;

    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    @Builder
    public KakaoNotification(User user, String phoneNumber, Boolean consent) {
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.consent = consent;
        this.status = ConnectionStatus.CONNECTED;
        this.consentedAt = LocalDateTime.now();
    }

    public void updateConsent(String phoneNumber, Boolean consent) {
        this.phoneNumber = phoneNumber;
        this.consent = consent;
        this.consentedAt = LocalDateTime.now();
    }

    public void disconnect() {
        this.status = ConnectionStatus.DISCONNECTED;
    }
}
