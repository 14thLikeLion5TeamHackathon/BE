package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.RegionMapper;
import likelion.madi.domain.Weather;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🌟 OpenWeatherMap 5일 예보 지원 범위 (오늘 포함 최대 5일: 0, 1, 2, 3, 4일 뒤)
    // 기상청 단기예보(3일) 기준이라면 3으로 변경하시면 됩니다.
    private static final int MAX_FORECAST_DAYS = 5;

    @Value("${openweathermap.api.key:1f90a4007ee2683ffb37f7c6786fa299}")
    private String apiKey;

    /**
     * 예보 제공 날짜 유효성 검증 메서드
     * - 오늘보다 과거 날짜이거나, 제공 가능한 범위를 벗어난 미래 날짜인 경우 400 Bad Request
     */
    private void validateForecastRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(today, date);

        if (daysDiff < 0 || daysDiff >= MAX_FORECAST_DAYS) {
            log.warn("예보 범위를 벗어난 날짜 요청: requestedDate={}, today={}, daysDiff={}", date, today, daysDiff);
            // 🌟 ErrorStatus 없이 String 메시지로 바로 던집니다!
            throw new BadRequestException("예보 제공 범위를 벗어난 날짜입니다.");
        }
    }

    @Transactional
    public WeatherResponseDto getWeather(Double lat, Double lon, String city, String district, LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        // 🌟 1. 날짜 유효성 검증 (과거 또는 예보 범위 초과 시 400 에러)
        validateForecastRange(date);

        Integer intLat = null;
        Integer intLon = null;

        if (lat != null && lon != null && Math.abs(lat) <= 90.0 && Math.abs(lon) <= 180.0) {
            intLat = (int) Math.round(lat);
            intLon = (int) Math.round(lon);
        }

        // 1. 지역명이 제공된 경우 (우선 탐색)
        if (city != null && district != null) {
            List<Weather> cachedWeatherList = weatherRepository.findByTargetDateAndCityAndDistrict(date, city, district);
            if (!cachedWeatherList.isEmpty()) {
                return new WeatherResponseDto(cachedWeatherList.get(0)); // 💡 중복 중 첫 번째 안전 반환
            }

            if (intLat == null || intLon == null) {
                RegionMapper.Coordinate coord = RegionMapper.getCoordinate(city, district);
                lat = coord.getLat();
                lon = coord.getLon();
                intLat = (int) Math.round(lat);
                intLon = (int) Math.round(lon);
            }

            return callOpenWeatherMapForecastApi(lat, lon, date, city, district, intLat, intLon, true);
        }

        // 2. 위경도(GPS)만 제공된 경우
        if (intLat != null && intLon != null) {
            List<Weather> cachedGpsWeatherList = weatherRepository.findByTargetDateAndLatitudeAndLongitude(date, intLat, intLon);
            if (!cachedGpsWeatherList.isEmpty()) {
                return new WeatherResponseDto(cachedGpsWeatherList.get(0)); // 💡 중복 중 첫 번째 안전 반환
            }
            return callOpenWeatherMapForecastApi(lat, lon, date, "GPS_USER", "lat_" + intLat + "_lon_" + intLon, intLat, intLon, true);
        }

        // 3. 둘 다 없는 경우 기본값 (서울 중구) 처리
        city = "서울특별시";
        district = "중구";
        RegionMapper.Coordinate defaultCoord = RegionMapper.getCoordinate(city, district);
        lat = defaultCoord.getLat();
        lon = defaultCoord.getLon();
        intLat = (int) Math.round(lat);
        intLon = (int) Math.round(lon);

        List<Weather> cachedDefaultList = weatherRepository.findByTargetDateAndCityAndDistrict(date, city, district);
        if (!cachedDefaultList.isEmpty()) {
            return new WeatherResponseDto(cachedDefaultList.get(0)); // 💡 중복 중 첫 번째 안전 반환
        }

        return callOpenWeatherMapForecastApi(lat, lon, date, city, district, intLat, intLon, true);
    }

    @Transactional
    public WeatherResponseDto getWeatherAndEnvironment(LocalDate targetDate, String city, String district) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        // 🌟 1. 날짜 유효성 검증 (과거 또는 예보 범위 초과 시 400 에러)
        validateForecastRange(date);

        String targetCity = (city != null) ? city : "서울특별시";
        String targetDistrict = (district != null) ? district : "중구";

        List<Weather> cachedWeatherList = weatherRepository.findByTargetDateAndCityAndDistrict(date, targetCity, targetDistrict);
        if (!cachedWeatherList.isEmpty()) {
            return new WeatherResponseDto(cachedWeatherList.get(0));
        }

        RegionMapper.Coordinate coord = RegionMapper.getCoordinate(targetCity, targetDistrict);
        return callOpenWeatherMapForecastApi(coord.getLat(), coord.getLon(), date, targetCity, targetDistrict,
                (int) Math.round(coord.getLat()), (int) Math.round(coord.getLon()), true);
    }

    private WeatherResponseDto callOpenWeatherMapForecastApi(double lat, double lon, LocalDate targetDate,
                                                             String city, String district, Integer intLat, Integer intLon, boolean shouldSaveDb) {

        URI uri = UriComponentsBuilder.fromUriString("https://api.openweathermap.org")
                .path("/data/2.5/forecast")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .encode()
                .build()
                .toUri();

        String jsonResponseStr;
        try {
            jsonResponseStr = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new RuntimeException("외부 예보 API 통신 실패: " + e.getMessage());
        }

        if (jsonResponseStr == null || jsonResponseStr.isEmpty()) {
            throw new RuntimeException("외부 예보 API 응답 데이터가 비어 있습니다.");
        }

        Double tempVal = null;
        String weatherCondition = null;

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponseStr);

            if (rootNode.has("list") && rootNode.get("list").isArray()) {
                JsonNode listNode = rootNode.get("list");
                JsonNode targetForecastNode = listNode.get(0);

                String targetDateStr = targetDate.toString();

                for (JsonNode node : listNode) {
                    String dtTxt = node.get("dt_txt").asText();
                    if (dtTxt.startsWith(targetDateStr)) {
                        if (dtTxt.contains("12:00:00")) {
                            targetForecastNode = node;
                            break;
                        }
                        targetForecastNode = node;
                    }
                }

                if (targetForecastNode.has("main") && targetForecastNode.get("main").has("temp")) {
                    tempVal = targetForecastNode.get("main").get("temp").asDouble();
                }

                if (targetForecastNode.has("weather") && targetForecastNode.get("weather").isArray() && targetForecastNode.get("weather").size() > 0) {
                    weatherCondition = targetForecastNode.get("weather").get(0).get("description").asText();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("예보 JSON 파싱 오류: " + e.getMessage());
        }

        String finalTemp = (tempVal != null) ? String.valueOf(tempVal) : "25.0";
        String finalCondition = (weatherCondition != null && !weatherCondition.isEmpty()) ? weatherCondition : "맑음";
        String finalUvIndex = "보통";

        // 8월 여름철 미세먼지 랜덤 범위(15~45)
        int finalPm10Value = (int) (Math.random() * 31) + 15;
        String finalPm10Status = (finalPm10Value <= 30) ? "좋음" : "보통";

        if (shouldSaveDb) {
            Weather newWeather = Weather.builder()
                    .targetDate(targetDate)
                    .city(city != null ? city : "GPS_USER")
                    .district(district != null ? district : "UNKNOWN")
                    .latitude(intLat)
                    .longitude(intLon)
                    .temperature(finalTemp)
                    .weatherCondition(finalCondition)
                    .pm10Status(finalPm10Status)
                    .pm10Value(finalPm10Value)
                    .uvIndex(finalUvIndex)
                    .build();

            weatherRepository.save(newWeather);
            return new WeatherResponseDto(newWeather);
        }

        return WeatherResponseDto.builder()
                .targetDate(targetDate.toString())
                .city(city)
                .district(district)
                .temperature(finalTemp)
                .weatherCondition(finalCondition)
                .uvIndex(finalUvIndex)
                .pm10Status(finalPm10Status)
                .pm10Value(finalPm10Value)
                .build();
    }
}