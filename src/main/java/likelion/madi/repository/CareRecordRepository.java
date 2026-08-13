package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.CareRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CareRecordRepository extends JpaRepository<CareRecord, Long> {
    List<CareRecord> findTop3ByCareCardOrderByRecordedAtDesc(CareCard careCard);

    List<CareRecord> findByCareCardOrderByRecordedAtAsc(CareCard careCard);

    Optional<CareRecord> findFirstByCareCardAndRecordedAtBeforeOrderByRecordedAtDesc(
            CareCard careCard, LocalDateTime recordedAt);
}