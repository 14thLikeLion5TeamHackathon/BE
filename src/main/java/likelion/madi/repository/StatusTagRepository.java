package likelion.madi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.StatusTag;

public interface StatusTagRepository extends JpaRepository<StatusTag, Long> {
    Optional<StatusTag> findByCode(String code);
}