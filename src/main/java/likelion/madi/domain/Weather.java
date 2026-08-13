package likelion.madi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_id")
    private Long weatherId;

    private LocalDate targetDate;

    private String city;
    private String district;

    private Integer latitude;
    private Integer longitude;

    private String temperature;
    private String weatherCondition;
    private String uvIndex;
    private String pm10Status;
    private Integer pm10Value;

    @Builder
    public Weather(LocalDate targetDate, String city, String district, Integer latitude, Integer longitude,
                   String temperature, String weatherCondition, String uvIndex, String pm10Status, Integer pm10Value) {
        this.targetDate = targetDate;
        this.city = city;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.uvIndex = uvIndex;
        this.pm10Status = pm10Status;
        this.pm10Value = pm10Value;
    }
}