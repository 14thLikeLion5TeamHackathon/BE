package likelion.madi.service;

import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.common.util.KstDate;
import likelion.madi.domain.*;
import likelion.madi.dto.request.CareCardCreateRequest;
import likelion.madi.dto.response.CareCardCreateResponse;
import likelion.madi.dto.response.CareCardDetailResponse;
import likelion.madi.dto.response.CareCardListResponse;
import likelion.madi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CareCardService {
    private static final int DAILY_FEEDBACK_QUOTA = 3;

    private final CareCardRepository careCardRepository;
    private final TreatmentRepository treatmentRepository;
    private final UserRepository userRepository;
    private final RecoveryGuideRepository recoveryGuideRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final TodayCareService todayCareService;
    private final CareRecordRepository careRecordRepository;

    // 카드 생성
    public CareCardCreateResponse create(Long userId, CareCardCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        CareCard careCard = CareCard.builder()
                .user(user)
                .treatmentDate(request.getTreatmentDate())
                .build();

        for (CareCardCreateRequest.TreatmentEntry entry : request.getTreatments()) {
            if (entry.getTreatmentId() == null && entry.getCustomName() == null) {
                throw new BadRequestException("treatment_id 또는 custom_name 중 하나는 입력해야 합니다.");
            }

            Treatment treatment = null;
            if (entry.getTreatmentId() != null) {
                treatment = treatmentRepository.findById(entry.getTreatmentId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_TREATMENT));
            }

            careCard.addTreatment(CareCardTreatment.builder()
                    .careCard(careCard)
                    .treatment(treatment)
                    .customName(entry.getCustomName())
                    .build());
        }

        careCardRepository.save(careCard);

        return CareCardCreateResponse.from(careCard);
    }

    // 케어카드 상세조회
    @Transactional(readOnly = true)
    public CareCardDetailResponse getDetail(Long userId, Long cardId, String city, String district) {
        CareCard careCard = careCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CARE_CARD));

        if (!careCard.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        // 대표 시술 결정 (??)
        CareCardTreatment primary = careCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();

        // 디데이 계산 (며칠 지났는지)
        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), KstDate.today());

        if (treatment == null) {
            return CareCardDetailResponse.builder()
                    .cardId(careCard.getCardId())
                    .treatmentName(primary.getCustomName())
                    .treatmentDate(careCard.getTreatmentDate())
                    .dDay(dDay)
                    .recoveryTotalDays(null)
                    .todayCare(List.of())
                    .feedbackQuota(buildFeedbackQuota(userId))
                    .visitedStore(null)
                    .build();
        }



        // 총 회복 기간 계산 (Treatment에서 직접 가져옴)
        Integer recoveryTotalDays = treatment.getRecoveryTotalDays();
        Integer recoveryTransitionDay = treatment.getRecoveryTransitionDay();

        AacStore store = treatment.getStore();

        return CareCardDetailResponse.builder()
                .cardId(careCard.getCardId())
                .treatmentName(treatment.getName())
                .treatmentDate(careCard.getTreatmentDate())
                .dDay(dDay)
                .recoveryTotalDays(recoveryTotalDays)
                .recoveryTransitionDay(recoveryTransitionDay)
                .todayCare(splitLines(todayCareService.generateOrGetTodayCare(careCard, treatment, dDay, city, district)))
                .feedbackQuota(buildFeedbackQuota(userId))
                .visitedStore(store == null ? null : CareCardDetailResponse.VisitedStore.builder()
                        .storeId(store.getStoreId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .url(store.getUrl())
                        .latitude(store.getLatitude())
                        .longitude(store.getLongitude())
                        .build())
                .build();
    }

    // AI 피드백 몇번 사용했는지
    private CareCardDetailResponse.FeedbackQuota buildFeedbackQuota(Long userId) {
        LocalDateTime startOfDay = KstDate.today().atStartOfDay();
        LocalDateTime endOfDay = KstDate.today().plusDays(1).atStartOfDay();
        long used = aiFeedbackRepository.countTodayByUser(userId, startOfDay, endOfDay);
        return CareCardDetailResponse.FeedbackQuota.builder()
                .used(used)
                .total(DAILY_FEEDBACK_QUOTA)
                .build();
    }

    // 텍스트 리스트 쪼개기
    private List<String> splitLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // 케어카드 목록 조회 (카드별 최근 기록 포함)
    @Transactional(readOnly = true)
    public List<CareCardListResponse> getMyCareCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        List<CareCard> careCards = careCardRepository.findByUser(user);
        List<CareCardListResponse> result = new ArrayList<>();

        for (CareCard careCard : careCards) {
            CareCardTreatment primary = careCard.getTreatments().get(0);
            Treatment treatment = primary.getTreatment();
            int cardDDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), KstDate.today());

            String treatmentName = treatment != null ? treatment.getName() : primary.getCustomName();
            Integer recoveryTotalDays = treatment != null ? treatment.getRecoveryTotalDays() : null;
            String cardStatus = (recoveryTotalDays != null && cardDDay >= recoveryTotalDays) ? "completed" : "active";

            CareCardListResponse.CareCardListResponseBuilder builder = CareCardListResponse.builder()
                    .cardId(careCard.getCardId())
                    .treatmentName(treatmentName)
                    .treatmentDate(careCard.getTreatmentDate())
                    .status(cardStatus)
                    .recoveryTotalDays(recoveryTotalDays);

            careRecordRepository.findTopByCareCardOrderByRecordedAtDesc(careCard).ifPresent(record -> {
                Map<String, Integer> intensityByTag = record.getTags().stream()
                        .collect(Collectors.toMap(t -> t.getStatusTag().getName(), CareRecordTag::getIntensity));

                CareCardListResponse.AiFeedbackItem aiFeedbackItem = aiFeedbackRepository.findByCareRecord(record)
                        .map(f -> CareCardListResponse.AiFeedbackItem.builder()
                                .feedbackId(f.getFeedbackId())
                                .changeSummary(f.getChangeSummary())
                                .careGuidance(f.getCareGuidance())
                                .needsConsultation(f.getNeedsConsultation())
                                .build())
                        .orElse(null);

                builder.recordId(record.getRecordId())
                        .recordedAt(record.getRecordedAt().toLocalDate())
                        .dDay(record.getDDay())
                        .photoUrls(record.getPhotoUrls())
                        .statusDescription(record.getStatusDescription())
                        .redness(intensityByTag.get("붉은기"))
                        .swelling(intensityByTag.get("부기"))
                        .pain(intensityByTag.get("통증"))
                        .dryness(intensityByTag.get("건조함"))
                        .aiFeedback(aiFeedbackItem);
            });

            result.add(builder.build());
        }

        return result;
    }
}
