package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Environment", description = "날씨 및 환경 지표 조회 API")
@RestController
@RequestMapping("/api/v1/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final WeatherService weatherService;

    @Operation(summary = "단건 날씨 및 환경 지표 조회", description = "위도/경도(GPS) 또는 지역명(시/도, 구/군) 및 대상 날짜별 실시간 예보를 조회합니다.")
    @GetMapping("/weather")
    public ResponseEntity<WeatherResponseDto> getWeather(
            @Parameter(description = "위도 (예: 37.5665)", example = "37.5665")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "경도 (예: 126.9780)", example = "126.9780")
            @RequestParam(required = false) Double lon,

            @Parameter(description = "시/도 단위 (예: 서울특별시)", example = "서울특별시")
            @RequestParam(required = false) String city,

            @Parameter(description = "구/군 단위 (예: 중구)", example = "중구")
            @RequestParam(required = false) String district,

            @Parameter(description = "조회 대상 날짜 (YYYY-MM-DD)", example = "2026-08-13")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate
    ) {
        WeatherResponseDto responseDto = weatherService.getWeather(lat, lon, city, district, targetDate);
        return ResponseEntity.ok(responseDto); // 💡 ApiResponse 없이 DTO 객체 바로 반환
    }
}