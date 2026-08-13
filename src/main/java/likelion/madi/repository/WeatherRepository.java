package likelion.madi.repository;

import likelion.madi.domain.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    // 지역명 기준 캐시 조회
    Optional<Weather> findByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);

    // 정수 위경도 기준 캐시 조회
    Optional<Weather> findByTargetDateAndLatitudeAndLongitude(LocalDate targetDate, Integer latitude, Integer longitude);

    // 💡 [추가 완료] WeatherLoader 등에서 사용하는 존재 여부 확인 쿼리 메서드
    boolean existsByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);
}