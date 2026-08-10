package likelion.madi.repository;

import likelion.madi.domain.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);
}