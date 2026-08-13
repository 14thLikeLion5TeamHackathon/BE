package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import likelion.madi.domain.CareChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareChecklistRepository extends JpaRepository<CareChecklist, Long> {
    List<CareChecklist> findByCareCard(CareCard careCard);
}