package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareChecklist;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CareChecklistRepository extends JpaRepository<CareChecklist, Long> {
    List<CareChecklist> findByCareCard(CareCard careCard);

    List<CareChecklist> findByCareCardAndCheckDate(CareCard careCard, LocalDate checkDate);

    // 카드가 없을 때(일정+날씨 기반) 생성된 항목 조회용
    List<CareChecklist> findByUserAndCareCardIsNullAndCheckDate(User user, LocalDate checkDate);
}