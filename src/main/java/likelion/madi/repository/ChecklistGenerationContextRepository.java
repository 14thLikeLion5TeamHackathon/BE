package likelion.madi.repository;

import likelion.madi.domain.ChecklistGenerationContext;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ChecklistGenerationContextRepository extends JpaRepository<ChecklistGenerationContext, Long> {

    Optional<ChecklistGenerationContext> findTopByUserAndTargetDateOrderByIdDesc(User user, LocalDate targetDate);
}
