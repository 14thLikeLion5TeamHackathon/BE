package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import likelion.madi.domain.Weather;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDate;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "위치를 특정할 수 없으면 null")
    private String city;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "위치를 특정할 수 없으면 null")
    private String district;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String temperature;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String weatherCondition;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String uvIndex;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String pm10Status;

    // 💡 누락되었던 미세먼지 정수 수치 필드 추가 (@JsonProperty로 스네이크 케이스 강제 매핑)
    @JsonProperty("pm10_value")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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