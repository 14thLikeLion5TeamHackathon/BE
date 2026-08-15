package likelion.madi.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLocationUpdateRequest {
    private Double latitude;
    private Double longitude;
}