package likelion.madi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChecklistUpdateRequest {

    @NotNull
    private Boolean completed;
}
