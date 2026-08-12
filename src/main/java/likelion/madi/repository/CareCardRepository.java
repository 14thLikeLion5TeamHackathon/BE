package likelion.madi.repository;

import likelion.madi.domain.CareCard;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareCardRepository extends JpaRepository<CareCard, Long> {
    List<CareCard> findByUser(User user);
}
