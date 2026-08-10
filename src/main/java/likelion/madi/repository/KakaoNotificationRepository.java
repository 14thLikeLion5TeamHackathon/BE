package likelion.madi.repository;


import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KakaoNotificationRepository extends JpaRepository<KakaoNotification, Long> {
    Optional<KakaoNotification> findByUser(User user);
}

