package likelion.madi.repository;

import likelion.madi.domain.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByNameContaining(String keyword);

    @Query("SELECT t FROM Treatment t " +
            "WHERE (:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:category IS NULL OR t.category = :category)")
    Page<Treatment> search(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);
}
