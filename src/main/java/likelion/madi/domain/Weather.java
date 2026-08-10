package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "weather", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"target_date", "city", "district"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String temperature;

    @Column(name = "weather_condition", nullable = false)
    private String weatherCondition;

    @Column(name = "uv_index", nullable = false)
    private String uvIndex;

    @Column(name = "pm10_value", nullable = false)
    private Integer pm10Value;

    @Column(name = "pm10_status", nullable = false)
    private String pm10Status;

    @Builder
    public Weather(LocalDate targetDate, String city, String district, String temperature,
                   String weatherCondition, String uvIndex, Integer pm10Value, String pm10Status) {
        this.targetDate = targetDate;
        this.city = city;
        this.district = district;
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.uvIndex = uvIndex;
        this.pm10Value = pm10Value;
        this.pm10Status = pm10Status;
    }
}