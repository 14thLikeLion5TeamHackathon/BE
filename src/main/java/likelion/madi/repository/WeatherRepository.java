package likelion.madi.repository;

import likelion.madi.domain.Weather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List; // 💡 List 임포트 추가

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    // 💡 단건 조회 시 중복 데이터로 인한 예외를 막기 위해 List로 반환하도록 수정
    List<Weather> findByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);

    List<Weather> findByTargetDateAndLatitudeAndLongitude(LocalDate targetDate, Double latitude, Double longitude);

    boolean existsByTargetDateAndCityAndDistrict(LocalDate targetDate, String city, String district);
}