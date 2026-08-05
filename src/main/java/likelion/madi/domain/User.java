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

    // 자체 계정 로그인 사용자만 값이 있고, 소셜 로그인 사용자는 null
    @Column(name = "login_id", unique = true, length = 50)
    private String loginId;

    // BCrypt 등으로 해시된 값만 저장 (평문 저장 금지)
    @Column(name = "password", length = 255)
    private String password;

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
    public User(String name, String loginId, String password) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
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

    // 이미 해시된 비밀번호를 전달받아 저장 (해싱은 서비스 레이어의 PasswordEncoder 책임)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
