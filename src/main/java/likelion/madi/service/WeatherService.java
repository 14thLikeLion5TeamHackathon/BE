package likelion.madi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.madi.common.exception.BadRequestException;
import likelion.madi.common.util.KstDate;
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

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // 🌟 OpenWeatherMap 5일 예보 지원 범위 (오늘 포함 최대 5일: 0, 1, 2, 3, 4일 뒤)
    private static final int MAX_FORECAST_DAYS = 5;

    // 🛡️ 1차 방어선 허용 범위 (예: 과거 30일 ~ 미래 7일)
    private static final int EXTREME_PAST_DAYS = -30;
    private static final int EXTREME_FUTURE_DAYS = 7;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    /**
     * 🛡️ 1차 방어선 (Fast-Fail): DB 조회 전 실행
     * 너무 먼 과거(30일 초과)나 미래(7일 초과)의 요청은 DB도 보지 않고 즉시 차단
     */
    private void validateExtremeDateRange(LocalDate date) {
        LocalDate today = KstDate.today();
        long daysDiff = ChronoUnit.DAYS.between(today, date);

        if (daysDiff < EXTREME_PAST_DAYS || daysDiff > EXTREME_FUTURE_DAYS) {
            log.warn("비정상적인 날짜 요청 (DB 조회 차단): requestedDate={}, today={}, daysDiff={}", date, today, daysDiff);
            throw new BadRequestException("조회할 수 있는 날짜 범위를 크게 벗어났습니다. (최대 과거 30일 ~ 미래 7일 지원)");
        }
    }

    /**
     * 🛡️ 2차 방어선: DB에 데이터가 없을 때, API 호출 전 실행
     * 오픈웨더맵이 제공 불가능한 과거이거나 5일 밖의 미래라면 에러 발생
     */
    private void validateForecastRange(LocalDate date) {
        LocalDate today = KstDate.today();
        long daysDiff = ChronoUnit.DAYS.between(today, date);

        if (daysDiff < 0 || daysDiff >= MAX_FORECAST_DAYS) {
            log.warn("예보 범위를 벗어난 날짜 요청: requestedDate={}, today={}, daysDiff={}", date, today, daysDiff);
            throw new BadRequestException("요청하신 날짜의 데이터가 DB에 존재하지 않으며, 오픈웨더맵 예보 지원 범위를 벗어나 새 데이터를 가져올 수 없습니다.");
        }
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String calculateUvIndex(String weatherCondition) {
        if (weatherCondition == null || weatherCondition.isBlank()) {
            return "보통";
        }

        String condition = weatherCondition.toLowerCase();
        int randomChance = random.nextInt(100);

        if (condition.contains("비") || condition.contains("눈") || condition.contains("소나기")
                || condition.contains("rain") || condition.contains("snow") || condition.contains("mist") || condition.contains("drizzle")) {
            return (randomChance < 90) ? "낮음" : "보통";
        }
        if (condition.contains("구름") || condition.contains("흐림") || condition.contains("cloud") || condition.contains("overcast")) {
            return (randomChance < 70) ? "보통" : "낮음";
        }
        if (condition.contains("맑음") || condition.contains("clear")) {
            return (randomChance < 70) ? "높음" : "매우 높음";
        }

        return "보통";
    }

    @Transactional
    public WeatherResponseDto getWeather(Double lat, Double lon, String city, String district, LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate : KstDate.today();

        // 🛡️ [1차 방어선] 터무니없는 날짜 요청은 입구에서 컷 (DB 보호)
        validateExtremeDateRange(date);

        if (lat != null && lon != null && Math.abs(lat) <= 90.0 && Math.abs(lon) <= 180.0) {
            double roundedLat = roundToTwoDecimals(lat);
            double roundedLon = roundToTwoDecimals(lon);

            // [DB 조회] 허용된 범위 내라면 일단 DB부터 확인
            List<Weather> cachedGpsWeather = weatherRepository.findByTargetDateAndLatitudeAndLongitude(date, roundedLat, roundedLon);
            if (!cachedGpsWeather.isEmpty()) {
                return new WeatherResponseDto(cachedGpsWeather.get(0));
            }

            // 🛡️ [2차 방어선] DB에 없으니 API를 불러야 하는데, API 허용 범위인지 검사
            validateForecastRange(date);

            return callOpenWeatherMapForecastApi(roundedLat, roundedLon, date, "GPS_USER", "lat_" + roundedLat + "_lon_" + roundedLon, roundedLat, roundedLon, true);
        }

        if (city != null && district != null) {
            List<Weather> cachedWeatherList = weatherRepository.findByTargetDateAndCityAndDistrict(date, city, district);
            if (!cachedWeatherList.isEmpty()) {
                return new WeatherResponseDto(cachedWeatherList.get(0));
            }

            validateForecastRange(date);

            RegionMapper.Coordinate coord = RegionMapper.getCoordinate(city, district);
            double roundedLat = roundToTwoDecimals(coord.getLat());
            double roundedLon = roundToTwoDecimals(coord.getLon());

            return callOpenWeatherMapForecastApi(roundedLat, roundedLon, date, city, district, roundedLat, roundedLon, true);
        }

        city = "서울특별시";
        district = "중구";
        RegionMapper.Coordinate defaultCoord = RegionMapper.getCoordinate(city, district);
        double roundedLat = roundToTwoDecimals(defaultCoord.getLat());
        double roundedLon = roundToTwoDecimals(defaultCoord.getLon());

        List<Weather> cachedDefaultList = weatherRepository.findByTargetDateAndCityAndDistrict(date, city, district);
        if (!cachedDefaultList.isEmpty()) {
            return new WeatherResponseDto(cachedDefaultList.get(0));
        }

        validateForecastRange(date);

        return callOpenWeatherMapForecastApi(roundedLat, roundedLon, date, city, district, roundedLat, roundedLon, true);
    }

    @Transactional
    public WeatherResponseDto getWeatherAndEnvironment(LocalDate targetDate, String city, String district) {
        LocalDate date = (targetDate != null) ? targetDate : KstDate.today();

        // 🛡️ 1차 방어선
        validateExtremeDateRange(date);

        String targetCity = (city != null) ? city : "서울특별시";
        String targetDistrict = (district != null) ? district : "중구";

        List<Weather> cachedWeatherList = weatherRepository.findByTargetDateAndCityAndDistrict(date, targetCity, targetDistrict);
        if (!cachedWeatherList.isEmpty()) {
            return new WeatherResponseDto(cachedWeatherList.get(0));
        }

        // 🛡️ 2차 방어선
        validateForecastRange(date);

        RegionMapper.Coordinate coord = RegionMapper.getCoordinate(targetCity, targetDistrict);
        double roundedLat = roundToTwoDecimals(coord.getLat());
        double roundedLon = roundToTwoDecimals(coord.getLon());

        return callOpenWeatherMapForecastApi(roundedLat, roundedLon, date, targetCity, targetDistrict, roundedLat, roundedLon, true);
    }

    private WeatherResponseDto callOpenWeatherMapForecastApi(double lat, double lon, LocalDate targetDate,
                                                             String city, String district, Double latToSave, Double lonToSave, boolean shouldSaveDb) {
        // ... 기존 callOpenWeatherMapForecastApi 내부 로직 동일 (생략 없이 그대로 유지) ...
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
        String finalUvIndex = calculateUvIndex(finalCondition);

        int finalPm10Value = (int) (Math.random() * 31) + 15;
        String finalPm10Status = (finalPm10Value <= 30) ? "좋음" : "보통";

        if (shouldSaveDb) {
            Weather newWeather = Weather.builder()
                    .targetDate(targetDate)
                    .city(city != null ? city : "GPS_USER")
                    .district(district != null ? district : "UNKNOWN")
                    .latitude(latToSave)
                    .longitude(lonToSave)
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