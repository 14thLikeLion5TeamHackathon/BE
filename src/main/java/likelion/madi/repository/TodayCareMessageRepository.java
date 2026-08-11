package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import likelion.madi.domain.TodayCareMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TodayCareMessageRepository extends JpaRepository<TodayCareMessage, Long> {
    Optional<TodayCareMessage> findByCareCardAndTargetDate(CareCard careCard, LocalDate targetDate);
}