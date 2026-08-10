package likelion.madi.repository;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User; // 💡 User 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoogleCalendarConnectionRepository extends JpaRepository<GoogleCalendarConnection, Long> {

    // 💡 이 메서드를 반드시 추가해 주세요!
    Optional<GoogleCalendarConnection> findByUser(User user);

}