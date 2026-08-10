package likelion.madi.controller;

import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Environment", description = "날씨 및 환경 지표 API")
@RestController
@RequiredArgsConstructor
public class EnvironmentController {

    private final WeatherService weatherService;

    @Operation(summary = "케어 브리핑 날씨 및 환경 지표 조회", description = "선택한 날짜와 지역의 날씨, 기온, 자외선, 미세먼지 정보를 조회합니다.")
    @GetMapping("/api/v1/environment/weather")
    public ResponseEntity<Map<String, Object>> getWeatherAndEnvironment(
            @RequestParam("target_date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
            @RequestParam("city") String city,
            @RequestParam("district") String district) {

        WeatherResponseDto weatherData = weatherService.getWeatherAndEnvironment(targetDate, city, district);

        Map<String, Object> response = Map.of(
                "success", true,
                "code", 200,
                "message", "환경 지표 및 날씨 정보 조회 성공",
                "data", weatherData
        );

        return ResponseEntity.ok(response);
    }
}