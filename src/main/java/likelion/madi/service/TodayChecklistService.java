package likelion.madi.service;

import likelion.madi.common.exception.ForbiddenException;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareCardTreatment;
import likelion.madi.domain.CareChecklist;
import likelion.madi.domain.Treatment;
import likelion.madi.domain.User;
import likelion.madi.dto.response.TodayChecklistResponse;
import likelion.madi.repository.CareCardRepository;
import likelion.madi.repository.CareChecklistRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayChecklistService {

    // 문장 끝 기준으로 쪼갬 (마침표, 느낌표, 물음표 등등...)
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    private final UserRepository userRepository;
    private final CareCardRepository careCardRepository;
    private final CareChecklistRepository careChecklistRepository;

    // 조회
    @Transactional
    public TodayChecklistResponse getTodayChecklist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        LocalDate today = LocalDate.now();
        List<CareCard> careCards = careCardRepository.findByUser(user);

        List<TodayChecklistResponse.Item> items = new ArrayList<>();
        for (CareCard careCard : careCards) {
            items.addAll(buildItemsForCard(careCard, today));
        }

        long completedCount = items.stream().filter(TodayChecklistResponse.Item::isCompleted).count();

        return TodayChecklistResponse.builder()
                .completedCount((int) completedCount)
                .totalCount(items.size())
                .items(items)
                .build();
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
