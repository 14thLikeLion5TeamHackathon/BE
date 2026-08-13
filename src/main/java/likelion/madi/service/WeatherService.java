package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.domain.Weather;
import likelion.madi.repository.WeatherRepository;
import likelion.madi.domain.RegionMapper;
import likelion.madi.dto.response.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweathermap.api.key:1f90a4007ee2683ffb37f7c6786fa299}")
    private String apiKey;

    @Transactional
    public WeatherResponseDto getWeather(Double lat, Double lon, String city, String district, LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        // 💡 [방어 로직] 위도나 경도가 비어있거나, 둘 중 하나만 들어온 경우 서울 중구 좌표로 안전하게 대체합니다.
        if (lat == null || lon == null || lat == 0.0 || lon == 0.0) {
            if (city != null && district != null) {
                RegionMapper.Coordinate coord = RegionMapper.getCoordinate(city, district);
                lat = coord.getLat();
                lon = coord.getLon();
            } else {
                RegionMapper.Coordinate defaultCoord = RegionMapper.getCoordinate("서울특별시", "중구");
                lat = defaultCoord.getLat();
                lon = defaultCoord.getLon();
            }
        }

        // 1. 지역명(시/도, 구/군)으로 조회하면서 유효한 좌표가 있는 경우 DB 캐시 확인
        if (city != null && district != null) {
            Optional<Weather> cachedWeather = weatherRepository.findByTargetDateAndCityAndDistrict(date, city, district);
            if (cachedWeather.isPresent()) {
                return new WeatherResponseDto(cachedWeather.get());
            }

            return callOpenWeatherMapForecastApi(lat, lon, date, city, district, true);
        }

        // 2. GPS (위도/경도) 모드 조회
        return callOpenWeatherMapForecastApi(lat, lon, date, null, null, false);
    }

    @Transactional
    public WeatherResponseDto getWeatherAndEnvironment(LocalDate targetDate, String city, String district) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        String targetCity = (city != null) ? city : "서울특별시";
        String targetDistrict = (district != null) ? district : "중구";

        Optional<Weather> cachedWeather = weatherRepository.findByTargetDateAndCityAndDistrict(date, targetCity, targetDistrict);
        if (cachedWeather.isPresent()) {
            return new WeatherResponseDto(cachedWeather.get());
        }

        RegionMapper.Coordinate coord = RegionMapper.getCoordinate(targetCity, targetDistrict);
        return callOpenWeatherMapForecastApi(coord.getLat(), coord.getLon(), date, targetCity, targetDistrict, true);
    }

    private WeatherResponseDto callOpenWeatherMapForecastApi(double lat, double lon, LocalDate targetDate, String city, String district, boolean shouldSaveDb) {

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

        System.out.println("🌐 [실제 전송 Forecast API URL]: " + uri.toString());

        String jsonResponseStr;
        try {
            jsonResponseStr = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            System.err.println("❌ 외부 예보 API 통신 실패 전문: " + e.getMessage());
            throw new RuntimeException("외부 예보 API 통신 실패: " + e.getMessage());
        }

        if (jsonResponseStr == null || jsonResponseStr.isEmpty()) {
            throw new RuntimeException("외부 예보 API 응답 데이터가 비어 있습니다.");
        }

        double tempVal = 25.0;
        String weatherCondition = "맑음";

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

        String tempStr = String.valueOf(tempVal);

        if (shouldSaveDb && city != null && district != null) {
            Weather newWeather = Weather.builder()
                    .targetDate(targetDate)
                    .city(city)
                    .district(district)
                    .temperature(tempStr)
                    .weatherCondition(weatherCondition)
                    .pm10Status("보통")
                    .pm10Value(40)
                    .uvIndex("보통")
                    .build();

            weatherRepository.save(newWeather);
            return new WeatherResponseDto(newWeather);
        }

        return WeatherResponseDto.builder()
                .targetDate(targetDate.toString())
                .city(city)
                .district(district)
                .temperature(tempStr)
                .weatherCondition(weatherCondition)
                .uvIndex("보통")
                .pm10Status("보통")
                .build();
    }
}