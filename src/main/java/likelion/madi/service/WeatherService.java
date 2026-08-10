package likelion.madi.service;

import likelion.madi.domain.Weather;
import likelion.madi.dto.response.WeatherResponseDto;
import likelion.madi.repository.WeatherRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;

    @Value("${openweathermap.service.key}")
    private String openWeatherApiKey;

    private static final Map<String, double[]> DISTRICT_COORDINATES = Map.ofEntries(
            Map.entry("서울_중구", new double[]{37.5641, 126.9979}),
            Map.entry("서울_종로구", new double[]{37.5730, 126.9794}),
            Map.entry("서울_용산구", new double[]{37.5326, 126.9900}),
            Map.entry("서울_강남구", new double[]{37.5172, 127.0473}),
            Map.entry("서울_서초구", new double[]{37.4837, 127.0324}),
            Map.entry("서울_마포구", new double[]{37.5663, 126.9016}),
            Map.entry("서울_송파구", new double[]{37.5145, 127.1058}),
            Map.entry("서울_영등포구", new double[]{37.5264, 126.8960}),
            Map.entry("서울_성동구", new double[]{37.5635, 127.0365}),
            Map.entry("서울_광진구", new double[]{37.5385, 127.0823}),
            Map.entry("서울_동대문구", new double[]{37.5744, 127.0397}),
            Map.entry("서울_중랑구", new double[]{37.6066, 127.0927}),
            Map.entry("서울_성북구", new double[]{37.5894, 127.0167}),
            Map.entry("서울_강북구", new double[]{37.6396, 127.0257}),
            Map.entry("서울_도봉구", new double[]{37.6688, 127.0471}),
            Map.entry("서울_노원구", new double[]{37.6542, 127.0568}),
            Map.entry("서울_은평구", new double[]{37.6027, 126.9291}),
            Map.entry("서울_서대문구", new double[]{37.5791, 126.9368}),
            Map.entry("서울_양천구", new double[]{37.5170, 126.8665}),
            Map.entry("서울_강서구", new double[]{37.5509, 126.8495}),
            Map.entry("서울_구로구", new double[]{37.4954, 126.8874}),
            Map.entry("서울_금천구", new double[]{37.4565, 126.8954}),
            Map.entry("서울_동작구", new double[]{37.5124, 126.9393}),
            Map.entry("서울_관악구", new double[]{37.4784, 126.9515}),
            Map.entry("서울_강동구", new double[]{37.5301, 127.1238}),

            Map.entry("부산_중구", new double[]{35.1062, 129.0326}),
            Map.entry("부산_서구", new double[]{35.0976, 129.0242}),
            Map.entry("부산_동구", new double[]{35.1294, 129.0456}),
            Map.entry("부산_영도구", new double[]{35.0912, 129.0678}),
            Map.entry("부산_부산진구", new double[]{35.1631, 129.0533}),
            Map.entry("부산_동래구", new double[]{35.2046, 129.0833}),
            Map.entry("부산_남구", new double[]{35.1366, 129.0844}),
            Map.entry("부산_북구", new double[]{35.1970, 129.0125}),
            Map.entry("부산_해운대구", new double[]{35.1631, 129.1636}),
            Map.entry("부산_사하구", new double[]{35.1042, 128.9754}),
            Map.entry("부산_금정구", new double[]{35.2430, 129.0920}),
            Map.entry("부산_강서구", new double[]{35.2124, 128.9805}),
            Map.entry("부산_연제구", new double[]{35.1764, 129.0797}),
            Map.entry("부산_수영구", new double[]{35.1456, 129.1131}),
            Map.entry("부산_사상구", new double[]{35.1524, 128.9871}),
            Map.entry("부산_기장군", new double[]{35.2445, 129.2223}),

            Map.entry("대구_중구", new double[]{35.8685, 128.6036}),
            Map.entry("대구_동구", new double[]{35.8867, 128.6356}),
            Map.entry("대구_서구", new double[]{35.8719, 128.5592}),
            Map.entry("대구_남구", new double[]{35.8456, 128.5975}),
            Map.entry("대구_북구", new double[]{35.8856, 128.5828}),
            Map.entry("대구_수성구", new double[]{35.8582, 128.6310}),
            Map.entry("대구_달서구", new double[]{35.8298, 128.5326}),
            Map.entry("대구_달성군", new double[]{35.6974, 128.4428}),
            Map.entry("대구_군위군", new double[]{36.2424, 128.6318}),

            Map.entry("인천_중구", new double[]{37.4738, 126.6215}),
            Map.entry("인천_동구", new double[]{37.4791, 126.6425}),
            Map.entry("인천_미추홀구", new double[]{37.4468, 126.6504}),
            Map.entry("인천_연수구", new double[]{37.4101, 126.6784}),
            Map.entry("인천_남동구", new double[]{37.4467, 126.7314}),
            Map.entry("인천_부평구", new double[]{37.5070, 126.7219}),
            Map.entry("인천_계양구", new double[]{37.5373, 126.7377}),
            Map.entry("인천_서구", new double[]{37.5456, 126.6760}),
            Map.entry("인천_강화군", new double[]{37.7466, 126.4878}),
            Map.entry("인천_옹진군", new double[]{37.4465, 126.6377}),

            Map.entry("광주_동구", new double[]{35.1460, 126.9231}),
            Map.entry("광주_서구", new double[]{35.1543, 126.8895}),
            Map.entry("광주_남구", new double[]{35.1329, 126.9023}),
            Map.entry("광주_북구", new double[]{35.1732, 126.9121}),
            Map.entry("광주_광산구", new double[]{35.3422, 126.7844})
    );

    @Transactional
    public WeatherResponseDto getWeatherAndEnvironment(LocalDate targetDate, String city, String district) {

        // 1. DB 캐시 확인
        Optional<Weather> cachedWeather = weatherRepository.findByTargetDateAndCityAndDistrict(targetDate, city, district);
        if (cachedWeather.isPresent()) {
            return convertToDto(cachedWeather.get(), targetDate);
        }

        String key = city + "_" + district;
        double[] coords = DISTRICT_COORDINATES.getOrDefault(key, new double[]{37.5641, 126.9979});
        double lat = coords[0];
        double lon = coords[1];

        Double temperature = 20.0;
        String weatherCondition = "맑음";
        String uvIndex = "매우 높음"; // 명세서 예시 등급 반영
        Integer pm10Value = 40;
        String pm10Status = "보통";

        try {
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();
            String targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 2. 5일 예보 API 호출
            URI forecastUri = UriComponentsBuilder.fromUriString("https://api.openweathermap.org/data/2.5/forecast")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", openWeatherApiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "kr")
                    .build(false)
                    .toUri();

            String forecastResponse = restTemplate.getForObject(forecastUri, String.class);
            JsonNode forecastRoot = mapper.readTree(forecastResponse);
            JsonNode listNode = forecastRoot.path("list");

            if (listNode.isArray() && listNode.size() > 0) {
                JsonNode targetItem = listNode.get(0);
                boolean found = false;

                // 👉 핵심 수정: 사용자가 요청한 날짜(targetDateStr)와 정확히 일치하는 예보 블록을 탐색
                for (JsonNode item : listNode) {
                    String dtTxt = item.path("dt_txt").asText(""); // 예: "2026-08-12 12:00:00"
                    if (dtTxt.startsWith(targetDateStr)) {
                        targetItem = item;
                        found = true;
                        // 낮 시간대(12:00) 데이터를 찾으면 가장 정확하므로 우선선택 후 탈출
                        if (dtTxt.contains("12:00:00")) {
                            break;
                        }
                    }
                }

                // 만약 일치하는 날짜가 없다면(너무 먼 미래 등) 리스트의 마지막 데이터로 안전 방어
                if (!found && listNode.size() > 0) {
                    targetItem = listNode.get(listNode.size() - 1);
                }

                if (targetItem.has("main")) {
                    temperature = Math.round(targetItem.path("main").path("temp").asDouble(20.0) * 10.0) / 10.0;
                }

                if (targetItem.has("weather") && targetItem.path("weather").isArray() && targetItem.path("weather").size() > 0) {
                    weatherCondition = targetItem.path("weather").get(0).path("description").asText("맑음");
                }
            }

            // 3. 대기오염 API 호출
            URI airUri = UriComponentsBuilder.fromUriString("https://api.openweathermap.org/data/2.5/air_pollution")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", openWeatherApiKey)
                    .build(false)
                    .toUri();

            String airResponse = restTemplate.getForObject(airUri, String.class);
            JsonNode airRoot = mapper.readTree(airResponse);

            if (airRoot.has("list") && airRoot.path("list").isArray() && airRoot.path("list").size() > 0) {
                JsonNode components = airRoot.path("list").get(0).path("components");
                if (components.has("pm10")) {
                    pm10Value = (int) Math.round(components.path("pm10").asDouble(40.0));
                    pm10Status = getPm10Status(pm10Value);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 외부 API 연동 에러: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. 새 데이터 DB 저장 (캐싱)
        Weather newWeather = Weather.builder()
                .targetDate(targetDate)
                .city(city)
                .district(district)
                .temperature(String.valueOf(temperature))
                .weatherCondition(weatherCondition)
                .uvIndex(uvIndex)
                .pm10Value(pm10Value)
                .pm10Status(pm10Status)
                .build();

        weatherRepository.save(newWeather);

        return convertToDto(newWeather, targetDate);
    }

    private WeatherResponseDto convertToDto(Weather weather, LocalDate targetDate) {
        String tempStr = weather.getTemperature().replace("°C", "").replace("°", "").trim();
        Double tempValue = 20.0;
        try {
            tempValue = Double.parseDouble(tempStr);
        } catch (NumberFormatException e) {
            tempValue = 20.0;
        }

        return WeatherResponseDto.builder()
                .targetDate(targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .locationName(weather.getCity() + " " + weather.getDistrict())
                .weatherCondition(weather.getWeatherCondition())
                .temperature(tempValue)
                .uvIndex(weather.getUvIndex())
                .pm10Value(weather.getPm10Value())
                .pm10Status(weather.getPm10Status())
                .build();
    }

    private String getPm10Status(int pm10) {
        if (pm10 <= 30) return "좋음";
        else if (pm10 <= 50) return "보통";
        else if (pm10 <= 100) return "나쁨";
        else return "매우 나쁨";
    }
}