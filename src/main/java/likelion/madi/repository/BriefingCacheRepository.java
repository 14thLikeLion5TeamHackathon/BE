package likelion.madi.repository;

import likelion.madi.domain.BriefingCache;
import likelion.madi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BriefingCacheRepository extends JpaRepository<BriefingCache, Long> {

    Optional<BriefingCache> findByUserAndTargetDateAndCityAndDistrictAndLatestRecordAtAndCardIds(
            User user, LocalDate targetDate, String city, String district, LocalDateTime latestRecordAt, String cardIds);
}