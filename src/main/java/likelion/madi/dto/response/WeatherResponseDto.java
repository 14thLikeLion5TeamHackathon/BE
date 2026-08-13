package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.madi.domain.Weather;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherResponseDto {

    private String targetDate;
    private String city;
    private String district;
    private String temperature;
    private String weatherCondition;
    private String uvIndex;
    private String pm10Status;

    // 💡 누락되었던 미세먼지 정수 수치 필드 추가 (@JsonProperty로 스네이크 케이스 강제 매핑)
    @JsonProperty("pm10_value")
    private Integer pm10Value;

    @Builder
    public WeatherResponseDto(String targetDate, String city, String district, String temperature,
                              String weatherCondition, String uvIndex, String pm10Status, Integer pm10Value) {
        this.targetDate = targetDate;
        this.city = city;
        this.district = district;
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
        this.uvIndex = uvIndex;
        this.pm10Status = pm10Status;
        this.pm10Value = pm10Value;
    }

    // Entity를 DTO로 변환하는 생성자
    public WeatherResponseDto(Weather weather) {
        this.targetDate = weather.getTargetDate() != null ? weather.getTargetDate().toString() : null;
        this.city = weather.getCity();
        this.district = weather.getDistrict();
        this.temperature = weather.getTemperature();
        this.weatherCondition = weather.getWeatherCondition();
        this.uvIndex = weather.getUvIndex();
        this.pm10Status = weather.getPm10Status();
        this.pm10Value = weather.getPm10Value() != null ? weather.getPm10Value() : 40; // null 방어 기본값
    }
}