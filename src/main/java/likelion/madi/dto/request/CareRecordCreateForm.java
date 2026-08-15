package likelion.madi.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CareRecordCreateForm {
    private List<MultipartFile> photo;
    private String statusDescription;
    private String tags;
}