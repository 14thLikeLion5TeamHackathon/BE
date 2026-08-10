package likelion.madi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KakaoNotificationRequest {
    @NotNull
    private Boolean consent;
}
