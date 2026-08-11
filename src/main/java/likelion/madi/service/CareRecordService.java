package likelion.madi.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import likelion.madi.dto.response.CareRecordTimelineResponse;
import likelion.madi.dto.response.StatusTagResponse;
import likelion.madi.repository.AiFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.ForbiddenException;
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
    private final AiFeedbackRepository aiFeedbackRepository;
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

    @Transactional(readOnly = true)
    public CareRecordTimelineResponse getTimeline(Long userId, Long cardId) {
        CareCard careCard = careCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_CARD));

        if (!careCard.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        List<CareRecord> records = careRecordRepository.findByCareCardOrderByRecordedAtAsc(careCard);

        List<CareRecordTimelineResponse.CareRecordTimelineItem> items = records.stream()
                .map(this::toTimelineItem)
                .toList();

        return CareRecordTimelineResponse.builder()
                .cardId(careCard.getCardId())
                .careRecords(items)
                .build();
    }

    private CareRecordTimelineResponse.CareRecordTimelineItem toTimelineItem(CareRecord record) {
        Map<String, Integer> intensityByTag = record.getTags().stream()
                .collect(Collectors.toMap(t -> t.getStatusTag().getName(), CareRecordTag::getIntensity));

        CareRecordTimelineResponse.AiFeedbackItem aiFeedbackItem = aiFeedbackRepository.findByCareRecord(record)
                .map(f -> CareRecordTimelineResponse.AiFeedbackItem.builder()
                        .feedbackId(f.getFeedbackId())
                        .changeSummary(f.getChangeSummary())
                        .careGuidance(f.getCareGuidance())
                        .needsConsultation(f.getNeedsConsultation())
                        .build())
                .orElse(null);

        return CareRecordTimelineResponse.CareRecordTimelineItem.builder()
                .recordId(record.getRecordId())
                .recordedAt(record.getRecordedAt().toLocalDate())
                .dDay(record.getDDay())
                .photoUrl(record.getPhotoUrl())
                .statusDescription(record.getStatusDescription())
                .redness(intensityByTag.get("붉은기"))
                .swelling(intensityByTag.get("부기"))
                .pain(intensityByTag.get("통증"))
                .dryness(intensityByTag.get("건조함"))
                .aiFeedback(aiFeedbackItem)
                .build();
    }
}