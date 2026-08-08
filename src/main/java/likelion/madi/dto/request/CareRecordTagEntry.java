package likelion.madi.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CareRecordTagEntry {
    private Long tagId;
    private Integer intensity;
}