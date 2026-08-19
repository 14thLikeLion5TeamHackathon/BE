package likelion.madi.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)

public class OnboardingResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isNewUser;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
