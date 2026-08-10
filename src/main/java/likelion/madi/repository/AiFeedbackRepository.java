package likelion.madi.repository;

import likelion.madi.domain.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

    @Query("SELECT COUNT(f) FROM AiFeedback f " +
            "WHERE f.careRecord.careCard.user.userId = :userId " +
            "AND f.createdAt >= :startOfDay AND f.createdAt < :endOfDay")
    long countTodayByUser(@Param("userId") Long userId,
                          @Param("startOfDay") LocalDateTime startOfDay,
                          @Param("endOfDay") LocalDateTime endOfDay);
}