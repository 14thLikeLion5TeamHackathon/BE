package likelion.madi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;


    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "agree_personal_info")
    private Boolean agreePersonalInfo;

    @Column(name = "agree_health_data")
    private Boolean agreeHealthData;

    @Column(name = "agree_calendar_data")
    private Boolean agreeCalendarData;

    @Column(name = "has_aac_offline_experience")
    private Boolean hasAacOfflineExperience;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public void completeOnboarding(LocalDate birthDate, String gender,
                                    Boolean agreePersonalInfo, Boolean agreeHealthData,
                                    Boolean agreeCalendarData, Boolean hasAacOfflineExperience) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.agreePersonalInfo = agreePersonalInfo;
        this.agreeHealthData = agreeHealthData;
        this.agreeCalendarData = agreeCalendarData;
        this.hasAacOfflineExperience = hasAacOfflineExperience;
    }


    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
