package likelion.madi.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import likelion.madi.common.util.KstDate;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareRecord;
import likelion.madi.domain.CareRecordPhoto;
import likelion.madi.domain.CareRecordTag;
import likelion.madi.domain.StatusTag;
import likelion.madi.dto.response.CareRecordResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.StatusTagRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CareRecordService {

    private static final int MAX_PHOTO_COUNT = 5;

    private final CareRecordRepository careRecordRepository;
    private final CareCardRepository careCardRepository;
    private final StatusTagRepository statusTagRepository;
    private final LocalFileStorageService fileStorageService;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    // 상태 기록 등록 API
    public CareRecordResponse create(Long cardId, List<MultipartFile> photos, String statusDescription, String tagsJson) {
        List<MultipartFile> validPhotos = photos == null ? List.of()
                : photos.stream().filter(p -> p != null && !p.isEmpty()).toList();
        boolean noPhoto = validPhotos.isEmpty();
        boolean noDescription = (statusDescription == null || statusDescription.isBlank());
        if (noPhoto && noDescription) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_RECORD_CONTENT_REQUIRED);
        }
        if (validPhotos.size() > MAX_PHOTO_COUNT) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_TOO_MANY_PHOTOS);
        }

        CareCard careCard = careCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_CARD));

        Map<String, Integer> tagIntensityByCode = parseTags(tagsJson);
        List<StatusTag> statusTags = statusTagRepository.findAll();
        validateAllTagsPresent(statusTags, tagIntensityByCode);

        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), KstDate.today());

        CareRecord careRecord = CareRecord.builder()
                .careCard(careCard)
                .statusDescription(statusDescription)
                .dDay(dDay)
                .build();

        careRecordRepository.save(careRecord);

        for (int i = 0; i < validPhotos.size(); i++) {
            String photoUrl = fileStorageService.store(validPhotos.get(i));
            careRecord.addPhoto(CareRecordPhoto.builder()
                    .careRecord(careRecord)
                    .photoUrl(photoUrl)
                    .sortOrder(i)
                    .build());
        }

        for (StatusTag statusTag : statusTags) {
            Integer intensity = tagIntensityByCode.get(statusTag.getCode());
            careRecord.addTag(new CareRecordTag(careRecord, statusTag, intensity));
        }

        return CareRecordResponse.from(careRecord);
    }

    private Map<String, Integer> parseTags(String tagsJson) {

        Map<String, Integer> tagIntensityByCode;

        try {
            tagIntensityByCode = objectMapper.readValue(tagsJson,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Integer.class));
        } catch (Exception e) {
            throw new BadRequestException(
                    "tags 형식이 올바르지 않습니다. 예: {\"redness\":2,\"swelling\":0,\"pain\":1,\"dryness\":3}");
        }

        for (Integer intensity : tagIntensityByCode.values()) {
            if (intensity == null || intensity < 0 || intensity > 3) {
                throw new BadRequestException("intensity는 0~3 사이 값이어야 합니다.");
            }
        }

        return tagIntensityByCode;
    }

    private void validateAllTagsPresent(List<StatusTag> statusTags, Map<String, Integer> tagIntensityByCode) {
        Set<String> requiredCodes = statusTags.stream()
                .map(StatusTag::getCode)
                .collect(Collectors.toSet());

        if (!tagIntensityByCode.keySet().containsAll(requiredCodes)) {
            throw new BadRequestException(ErrorStatus.BAD_REQUEST_SYMPTOM_TAGS_REQUIRED);
        }
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
                .photoUrls(record.getPhotoUrls())
                .statusDescription(record.getStatusDescription())
                .redness(intensityByTag.get("붉은기"))
                .swelling(intensityByTag.get("부기"))
                .pain(intensityByTag.get("통증"))
                .dryness(intensityByTag.get("건조함"))
                .aiFeedback(aiFeedbackItem)
                .build();
    }
}