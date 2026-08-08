package likelion.madi.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CareRecordCreateForm {
    private MultipartFile photo;
    private String statusDescription;
    private String tags;
}