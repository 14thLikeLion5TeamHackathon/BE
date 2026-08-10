package likelion.madi.common.config;

import likelion.madi.domain.AacStore;
import likelion.madi.domain.Treatment;
import likelion.madi.repository.AacStoreRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreatmentSeedLoader implements CommandLineRunner {
    private static final String CSV_PATH = "data/treatments.csv";

    private final TreatmentRepository treatmentRepository;
    private final AacStoreRepository aacStoreRepository;

    @Override
    public void run(String... args) throws Exception {
        if (treatmentRepository.count() > 0) {
            log.info("Treatment 데이터 이미 존재 - 시드 로딩 스킵");
            return;
        }

        Map<String, AacStore> storeCache = new HashMap<>();

        List<Treatment> treatments = new ArrayList<>();
        InputStream is = new ClassPathResource(CSV_PATH).getInputStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 6) {
                    continue;
                }
                // 컬럼 순서: 매장, 매장위치, 카테고리, 세부내용, 시술 설명, 매장URL
                String storeName = cols.get(0);
                String storeLocation = cols.get(1);
                String category = cols.get(2);
                String name = cols.get(3);
                String description = cols.get(4);
                String storeUrl = cols.get(5);

                AacStore store = resolveStore(storeCache, storeName, storeLocation, storeUrl);

                treatments.add(Treatment.builder()
                        .name(name)
                        .category(category)
                        .description(description)
                        .store(store)
                        .build());
            }
        }

        treatmentRepository.saveAll(treatments);
        log.info("Treatment 시드 데이터 {}건 로딩 완료 (매장 {}곳)", treatments.size(), storeCache.size());
    }

    private AacStore resolveStore(Map<String, AacStore> cache, String name, String address, String url) {
        String key = name + "|" + address;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        AacStore store = aacStoreRepository.findByNameAndAddress(name, address)
                .orElseGet(() -> aacStoreRepository.save(
                        AacStore.builder()
                                .name(name)
                                .address(address)
                                .url(url)
                                .build()
                ));
        cache.put(key, store);
        return store;
    }

    // 따옴표로 감싼 콤마를 안전하게 처리하는 간단한 CSV 파서
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