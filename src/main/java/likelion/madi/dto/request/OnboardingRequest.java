package likelion.madi.dto.request;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class OnboardingRequest {
    @NotBlank
    private String name;
    @NotNull
    private LocalDate birthDate;
    private String gender;
    @NotNull @AssertTrue
    private Boolean agreePersonalInfo;
    @NotNull @AssertTrue
    private Boolean agreeHealthData;
    private Boolean agreeCalendarData;
    @NotNull
    private Boolean hasAacOfflineExperience;



}
