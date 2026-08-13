package likelion.madi.controller;

import likelion.madi.common.response.ApiResponse;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public ResponseEntity<ApiResponse<WeatherResponseDto>> getWeather(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) @DateTimeFormat(pattern = "YYYY-MM-DD") LocalDate targetDate
    ) {
        // 서비스로 lat, lon, city, district가 누락 없이 그대로 전달되어야 합니다!
        WeatherResponseDto responseDto = weatherService.getWeather(lat, lon, city, district, targetDate);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}