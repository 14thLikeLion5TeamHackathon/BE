package likelion.madi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ScheduleUpdateRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDate eventDate;

    private LocalTime eventTime;

    private String location;
}
