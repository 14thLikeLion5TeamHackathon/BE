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
@Table(name = "GOOGLE_CALENDAR_CONNECTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleCalendarConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Long connectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "google_email", length = 100)
    private String googleEmail;

    @Column(name = "access_token", length = 255)
    private String accessToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ConnectionStatus status;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Builder
    public GoogleCalendarConnection(User user, String googleEmail, String accessToken, String refreshToken) {
        this.user = user;
        this.googleEmail = googleEmail;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.status = ConnectionStatus.CONNECTED;
        this.connectedAt = LocalDateTime.now();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void reconnect(String googleEmail, String accessToken, String refreshToken) {
        this.googleEmail = googleEmail;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.status = ConnectionStatus.CONNECTED;
        this.connectedAt = LocalDateTime.now();
    }

    public void disconnect() {
        this.status = ConnectionStatus.DISCONNECTED;
        this.accessToken = null;
        this.refreshToken = null;
    }
}
