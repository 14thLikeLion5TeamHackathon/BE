package likelion.madi.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;
    private LocalDate birthDate;
    private String gender;
}