package likelion.madi.repository;

import likelion.madi.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByNameContaining(String keyword);
}
