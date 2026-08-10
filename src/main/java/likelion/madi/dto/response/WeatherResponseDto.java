package likelion.madi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherResponseDto {

    @JsonProperty("target_date")
    private String targetDate;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("weather_condition")
    private String weatherCondition;

    private Double temperature; // 소수점 포함 숫자형태 (예: 28.5)

    @JsonProperty("uv_index")
    private String uvIndex;

    @JsonProperty("pm10_value")
    private Integer pm10Value; // 정수형 숫자 (예: 45)

    @JsonProperty("pm10_status")
    private String pm10Status;
}