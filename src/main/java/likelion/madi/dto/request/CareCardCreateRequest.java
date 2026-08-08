package likelion.madi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CareCardCreateRequest {

    @NotNull
    private LocalDate treatmentDate;

    @NotEmpty
    @Valid
    private List<TreatmentEntry> treatments;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TreatmentEntry {
        private Long treatmentId;
        private String customName;
        }
}
