package likelion.madi.service;

import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.AiFeedback;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.CareChecklist;
import likelion.madi.domain.CareRecordTag;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.response.TodayChecklistResponse;
import likelion.madi.repository.AiFeedbackRepository;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareChecklistRepository;
import likelion.madi.repository.CareRecordRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayChecklistService {

    // 문장 끝 기준으로 쪼갬 (마침표, 느낌표, 물음표 등등...)
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    // 오늘의 체크리스트에 한 번에 보여줄 최대 항목 수 (케어카드가 여러 개일 때 현재 상태가 급한 카드부터 채움)
    private static final int MAX_VISIBLE_ITEMS = 3;

    private final UserRepository userRepository;
    private final CareCardRepository careCardRepository;
    private final CareChecklistRepository careChecklistRepository;
    private final CareRecordRepository careRecordRepository;
    private final AiFeedbackRepository aiFeedbackRepository;

    // 조회
    @Transactional
    public TodayChecklistResponse getTodayChecklist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        LocalDate today = LocalDate.now();
        List<CareCard> careCards = careCardRepository.findByUser(user);

        // 체크리스트 항목은 카드별로 매일 생성/유지되어야 하므로, 노출 여부와 무관하게 모든 카드에 대해 만들어둔다.
        Map<CareCard, List<TodayChecklistResponse.Item>> itemsByCard = new LinkedHashMap<>();
        for (CareCard careCard : careCards) {
            itemsByCard.put(careCard, buildItemsForCard(careCard, today));
        }

        List<CareCard> cardsByUrgency = careCards.stream()
                .sorted(Comparator.comparingInt(this::computeUrgencyScore).reversed())
                .toList();

        List<TodayChecklistResponse.Item> visibleItems = new ArrayList<>();
        for (CareCard careCard : cardsByUrgency) {
            for (TodayChecklistResponse.Item item : itemsByCard.get(careCard)) {
                if (visibleItems.size() >= MAX_VISIBLE_ITEMS) {
                    break;
                }
                visibleItems.add(item);
            }
        }

        long completedCount = visibleItems.stream().filter(TodayChecklistResponse.Item::isCompleted).count();

        return TodayChecklistResponse.builder()
                .completedCount((int) completedCount)
                .totalCount(visibleItems.size())
                .items(visibleItems)
                .build();
    }

    // 케어카드의 현재 상태 긴급도 점수: AI가 병원 문의를 권고했으면 최우선, 아니면 최근 기록의 증상 강도(0~3) 중 최댓값, 기록이 없으면 0
    private int computeUrgencyScore(CareCard careCard) {
        return careRecordRepository.findTopByCareCardOrderByRecordedAtDesc(careCard)
                .map(record -> {
                    boolean needsConsultation = aiFeedbackRepository.findByCareRecord(record)
                            .map(AiFeedback::getNeedsConsultation)
                            .orElse(false);
                    if (needsConsultation) {
                        return 100;
                    }
                    return record.getTags().stream()
                            .mapToInt(CareRecordTag::getIntensity)
                            .max()
                            .orElse(0);
                })
                .orElse(0);
    }

    // 수정 (체크/해제)
    @Transactional
    public TodayChecklistResponse.Item updateChecklist(Long userId, Long checklistId, boolean completed) {
        CareChecklist checklist = careChecklistRepository.findById(checklistId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_CHECKLIST));

        CareCard careCard = checklist.getCareCard();
        if (!careCard.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorStatus.FORBIDDEN_RESOURCE_ACCESS);
        }

        if (completed) {
            checklist.check();
        } else {
            checklist.uncheck();
        }

        String sourceLabel = resolveSourceLabel(careCard, checklist.getCheckDate());

        return TodayChecklistResponse.Item.builder()
                .checklistId(checklist.getChecklistId())
                .label(checklist.getLabel())
                .sourceLabel(sourceLabel)
                .completed(Boolean.TRUE.equals(checklist.getIsChecked()))
                .build();
    }

    private String resolveSourceLabel(CareCard careCard, LocalDate checkDate) {
        if (careCard.getTreatments().isEmpty()) {
            return null;
        }
        CareCardTreatment primary = careCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        String name = treatment != null ? treatment.getName() : primary.getCustomName();
        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), checkDate);
        return name + " D+" + dDay;
    }

    private List<TodayChecklistResponse.Item> buildItemsForCard(CareCard careCard, LocalDate today) {
        if (careCard.getTreatments().isEmpty()) {
            return List.of();
        }

        CareCardTreatment primary = careCard.getTreatments().get(0);
        Treatment treatment = primary.getTreatment();
        if (treatment == null || treatment.getTodayCare() == null || treatment.getTodayCare().isBlank()) {
            return List.of();
        }

        int dDay = (int) ChronoUnit.DAYS.between(careCard.getTreatmentDate(), today);
        Integer recoveryTotalDays = treatment.getRecoveryTotalDays();
        boolean active = dDay >= 0 && (recoveryTotalDays == null || dDay <= recoveryTotalDays);
        if (!active) {
            return List.of();
        }

        String sourceLabel = resolveSourceLabel(careCard, today);
        List<String> sentences = splitIntoSentences(treatment.getTodayCare());

        Map<String, CareChecklist> existingByLabel = careChecklistRepository
                .findByCareCardAndCheckDate(careCard, today).stream()
                .collect(Collectors.toMap(CareChecklist::getLabel, c -> c, (a, b) -> a));

        List<TodayChecklistResponse.Item> items = new ArrayList<>();
        for (String sentence : sentences) {
            CareChecklist checklist = existingByLabel.get(sentence);
            if (checklist == null) {
                checklist = careChecklistRepository.save(CareChecklist.builder()
                        .careCard(careCard)
                        .checkDate(today)
                        .label(sentence)
                        .build());
            }

            items.add(TodayChecklistResponse.Item.builder()
                    .checklistId(checklist.getChecklistId())
                    .label(checklist.getLabel())
                    .sourceLabel(sourceLabel)
                    .completed(Boolean.TRUE.equals(checklist.getIsChecked()))
                    .build());
        }
        return items;
    }

    private List<String> splitIntoSentences(String text) {
        return SENTENCE_SPLIT.splitAsStream(text.trim())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
