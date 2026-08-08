package likelion.madi.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import likelion.madi.dto.response.StatusTagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareRecord;
import likelion.madi.domain.CareRecordTag;
import likelion.madi.domain.StatusTag;
import likelion.madi.dto.request.CareRecordTagEntry;
import likelion.madi.dto.response.CareRecordResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.StatusTagRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CareRecordService {

    private final CareRecordRepository careRecordRepository;
    private final CareCardRepository careCardRepository;
    private final StatusTagRepository statusTagRepository;
    private final LocalFileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    // 상태 기록 등록 API
    public CareRecordResponse create(Long cardId, MultipartFile photo, String statusDescription, String tagsJson) {
        boolean noPhoto = (photo == null || photo.isEmpty());
        boolean noDescription = (statusDescription == null || statusDescription.isBlank());
        if (noPhoto && noDescription) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_RECORD_CONTENT_REQUIRED);
        }

        CareCard careCard = careCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_CARD));

        String photoUrl = noPhoto ? null : fileStorageService.store(photo);
        List<CareRecordTagEntry> tagEntries = parseTags(tagsJson);

        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), LocalDate.now());

        CareRecord careRecord = CareRecord.builder()
                .careCard(careCard)
                .photoUrl(photoUrl)
                .statusDescription(statusDescription)
                .dDay(dDay)
                .build();

        careRecordRepository.save(careRecord);

        for (CareRecordTagEntry entry : tagEntries) {
            StatusTag statusTag = statusTagRepository.findById(entry.getTagId())
                    .orElseThrow(() -> new BadRequestException("존재하지 않는 상태 태그입니다."));

            careRecord.addTag(new CareRecordTag(careRecord, statusTag, entry.getIntensity()));
        }

        return CareRecordResponse.from(careRecord);
    }

    private List<CareRecordTagEntry> parseTags(String tagsJson) {

        List <CareRecordTagEntry> tagEntries;

        try {
           tagEntries=objectMapper.readValue(tagsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CareRecordTagEntry.class));
        } catch (Exception e) {
            throw new BadRequestException("tags 형식이 올바르지 않습니다. 예: [{\"tagId\":1,\"intensity\":2}]");
        }

        for (CareRecordTagEntry entry : tagEntries) {
            if (entry.getIntensity() == null || entry.getIntensity() < 0 || entry.getIntensity() > 3) {
                throw new BadRequestException("intensity는 0~3 사이 값이어야 합니다.");
            }
        }

        return tagEntries;
    }

    // 상태 태그 관련 서비스
    @Transactional(readOnly = true)
    public List<StatusTagResponse> getStatusTags() {
        return statusTagRepository.findAll().stream()
                .map(StatusTagResponse::from)
                .toList();
    }
}