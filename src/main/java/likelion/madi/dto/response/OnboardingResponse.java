package likelion.madi.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import likelion.madi.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)

public class OnboardingResponse {
    private Long userId;
    private String name;
    private Boolean isNewUser;
    private LocalDateTime createdAt;

    public static OnboardingResponse from(User user, boolean isNewUser) {
        return OnboardingResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .isNewUser(isNewUser)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
