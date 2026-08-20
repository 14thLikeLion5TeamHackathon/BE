package likelion.madi.repository;

import likelion.madi.domain.ChecklistGenerationContext;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ChecklistGenerationContextRepository extends JpaRepository<ChecklistGenerationContext, Long> {

    Optional<ChecklistGenerationContext> findTopByUserAndTargetDateOrderByIdDesc(User user, LocalDate targetDate);

    // 회원탈퇴 시 user_id 외래키 제약으로 유저 삭제가 막히지 않도록 정리
    void deleteByUser(User user);
}
