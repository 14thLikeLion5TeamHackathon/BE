package likelion.madi.common.config;

import likelion.madi.repository.WeatherRepository; // 본인의 WeatherRepository 경로로 수정!
import likelion.madi.domain.Weather; // 본인의 Weather Entity 경로로 수정!
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
public class WeatherLoader implements CommandLineRunner {

    private final WeatherRepository weatherRepository;

    public WeatherLoader(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/weather_data.csv");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // 첫 줄(헤더)은 건너뜀
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 8) continue;

                LocalDate targetDate = LocalDate.parse(values[0].trim());
                String city = values[1].trim();
                String district = values[2].trim();
                String temperature = values[3].trim();
                String weatherCondition = values[4].trim();
                String uvIndex = values[5].trim();
                int pm10Value = Integer.parseInt(values[6].trim());
                String pm10Status = values[7].trim();

                // 이미 해당 데이터가 존재하지 않을 때만 안전하게 저장 (중복 방지)
                boolean exists = weatherRepository.existsByTargetDateAndCityAndDistrict(targetDate, city, district);
                if (!exists) {
                    Weather weather = Weather.builder()
                            .targetDate(targetDate)
                            .city(city)
                            .district(district)
                            .temperature(temperature)
                            .weatherCondition(weatherCondition)
                            .uvIndex(uvIndex)
                            .pm10Value(pm10Value)
                            .pm10Status(pm10Status)
                            .build();

                    weatherRepository.save(weather);
                }
            }
            System.out.println("🌿 [WeatherLoader] 초기 날씨 데이터 적재 완료!");
        } catch (Exception e) {
            System.out.println("⚠️ [WeatherLoader] 이미 데이터가 존재하거나 적재 중 스킵됨: " + e.getMessage());
        }
    }
}