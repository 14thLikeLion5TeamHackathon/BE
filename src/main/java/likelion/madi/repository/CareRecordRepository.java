package likelion.madi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.CareRecord;

public interface CareRecordRepository extends JpaRepository<CareRecord, Long> {
}