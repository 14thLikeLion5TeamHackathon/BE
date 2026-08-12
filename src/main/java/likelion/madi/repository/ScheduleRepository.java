package likelion.madi.repository;

import likelion.madi.domain.Schedule;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserAndEventDate(User user, LocalDate eventDate);
}