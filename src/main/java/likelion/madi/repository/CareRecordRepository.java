package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.CareRecord;

import java.util.List;

public interface CareRecordRepository extends JpaRepository<CareRecord, Long> {
    List<CareRecord> findTop3ByCareCardOrderByRecordedAtDesc(CareCard careCard);
}