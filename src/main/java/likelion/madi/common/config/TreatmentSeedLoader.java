package likelion.madi.common.config;

import likelion.madi.domain.Treatment;
import likelion.madi.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreatmentSeedLoader implements CommandLineRunner {
    private static final String CSV_PATH = "data/treatments.csv";

    private final TreatmentRepository treatmentRepository;

    @Override
    public void run(String... args) throws Exception {
        if (treatmentRepository.count() > 0) {
            log.info("Treatment 데이터 이미 존재 - 시드 로딩 스킵");
            return;
        }

        List<Treatment> treatments = new ArrayList<>();
        InputStream is = new ClassPathResource(CSV_PATH).getInputStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵
            // TreatmentSeedLoader.java
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 5) {
                    continue;
                }
                // 여기 ↓ 이 블록을 기존 내용과 바꿔치기
                String storeName = cols.get(0);
                String storeLocation = cols.get(1);
                String category = cols.get(2);
                String name = cols.get(3);
                String description = cols.get(4);

                treatments.add(Treatment.builder()
                        .name(name)
                        .category(category)
                        .description(description)
                        .storeName(storeName)
                        .storeLocation(storeLocation)
                        .build());
            }
        }

        treatmentRepository.saveAll(treatments);
        log.info("Treatment 시드 데이터 {}건 로딩 완료", treatments.size());
    }

    // 따옴표로 감싼 콤마(예: "디자인 제모(목뒤, 헤어라인, 구레나룻) 1회")를 안전하게 처리하는 간단한 CSV 파서
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result;
    }
}
