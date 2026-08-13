package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.madi.domain.Weather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponseDto {

    @JsonProperty("target_date")
    private String targetDate;

    private String city;

    private String district;

    private String temperature;

    @JsonProperty("weather_condition")
    private String weatherCondition;

    @JsonProperty("uv_index")
    private String uvIndex; // 👈 TodayCareService 호환용 추가

    @JsonProperty("pm10_status")
    private String pm10Status; // 👈 TodayCareService 호환용 추가

    // Weather 엔티티 기반 변환 생성자
    public WeatherResponseDto(Weather weather) {
        this.targetDate = weather.getTargetDate() != null ? weather.getTargetDate().toString() : null;
        this.city = weather.getCity();
        this.district = weather.getDistrict();
        this.temperature = weather.getTemperature() != null ? String.valueOf(weather.getTemperature()) : null;
        this.weatherCondition = weather.getWeatherCondition();
        // Weather 엔티티에 해당 필드가 있다면 꺼내오고, 없으면 기본값 설정
        this.uvIndex = "보통";
        this.pm10Status = "보통";
    }
}