package likelion.madi.common.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import likelion.madi.domain.CareRecord;
import likelion.madi.domain.CareRecordPhoto;
import likelion.madi.repository.CareRecordRepository;
import lombok.RequiredArgsConstructor;

// 사진 여러 장 지원 이전에 만들어진 기록(photo_url 컬럼에 사진 1장)을 CareRecordPhoto로 옮겨줌
@Component
@RequiredArgsConstructor
public class CareRecordPhotoMigrationLoader implements CommandLineRunner {

    private final CareRecordRepository careRecordRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<CareRecord> legacyRecords = careRecordRepository.findAll().stream()
                .filter(record -> record.getPhotoUrl() != null && record.getPhotos().isEmpty())
                .toList();

        for (CareRecord record : legacyRecords) {
            record.addPhoto(CareRecordPhoto.builder()
                    .careRecord(record)
                    .photoUrl(record.getPhotoUrl())
                    .sortOrder(0)
                    .build());
        }
    }
}
