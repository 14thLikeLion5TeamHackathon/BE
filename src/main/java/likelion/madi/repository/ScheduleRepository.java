package likelion.madi.repository;

import likelion.madi.domain.Schedule;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}