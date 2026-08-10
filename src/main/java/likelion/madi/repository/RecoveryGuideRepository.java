package likelion.madi.repository;

import likelion.madi.domain.RecoveryGuide;
import likelion.madi.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecoveryGuideRepository extends JpaRepository<RecoveryGuide, Long> {

    @Query("SELECT r FROM RecoveryGuide r WHERE r.treatment = :treatment ORDER BY r.dDayMin ASC")
    List<RecoveryGuide> findByTreatmentOrderByDDayMinAsc(@Param("treatment") Treatment treatment);
}