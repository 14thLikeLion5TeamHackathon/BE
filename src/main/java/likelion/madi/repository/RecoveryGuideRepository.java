package likelion.madi.repository;

import likelion.madi.domain.RecoveryGuide;
import likelion.madi.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecoveryGuideRepository extends JpaRepository<RecoveryGuide, Long> {
    List<RecoveryGuide> findByTreatmentOrderByDDayMinAsc(Treatment treatment);
}