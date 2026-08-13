package likelion.madi.repository;

import likelion.madi.domain.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    // 1. 날짜, 시/도, 구/군 조건으로 특정 날씨 정보 조회
    Optional<Weather> findByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);

    // 2. 🚨 지금 에러가 난 부분! 이 메서드가 반드시 있어야 합니다. (중복 체크용)
    boolean existsByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);

    Optional<Weather> findByTargetDate(LocalDate targetDate);
}