package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "city", length = 30)
    private String city;

    @Column(name = "district", length = 30)
    private String district;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareCard> careCards = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KakaoNotification> kakaoNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoogleCalendarConnection> googleCalendarConnections = new ArrayList<>();

    @Builder
    public User(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public void completeOnboarding(String name, LocalDate birthDate, String gender,
                                    Boolean agreePersonalInfo, Boolean agreeHealthData,
                                    Boolean agreeCalendarData, Boolean hasAacOfflineExperience) {
        this.name = name;
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

    public void updatePartial(String name, LocalDate birthDate, String gender) {
        if (name != null) this.name = name;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
    }

    public void updateLocation(Double latitude, Double longitude, String city, String district) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.district = district;
    }
}
