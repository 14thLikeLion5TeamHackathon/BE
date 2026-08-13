package likelion.madi.dto.response;

import likelion.madi.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInfoResponse {
    private Long userId;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private Boolean hasAacOfflineExperience;
    private Boolean agreePersonalInfo;
    private Boolean agreeHealthData;
    private Boolean agreeCalendarData;

    @Builder
    public UserInfoResponse(Long userId, String name, LocalDate birthDate, String gender,
                            Boolean hasAacOfflineExperience, Boolean agreePersonalInfo,
                            Boolean agreeHealthData, Boolean agreeCalendarData) {
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.hasAacOfflineExperience = hasAacOfflineExperience;
        this.agreePersonalInfo = agreePersonalInfo;
        this.agreeHealthData = agreeHealthData;
        this.agreeCalendarData = agreeCalendarData;
    }

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .hasAacOfflineExperience(user.getHasAacOfflineExperience())
                .agreePersonalInfo(user.getAgreePersonalInfo())
                .agreeHealthData(user.getAgreeHealthData())
                .agreeCalendarData(user.getAgreeCalendarData())
                .build();
    }
}