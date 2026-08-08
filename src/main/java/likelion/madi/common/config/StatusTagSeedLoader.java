package likelion.madi.common.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import likelion.madi.domain.StatusTag;
import likelion.madi.repository.StatusTagRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatusTagSeedLoader implements CommandLineRunner {

    private final StatusTagRepository statusTagRepository;

    @Override
    public void run(String... args) {
        if (statusTagRepository.count() > 0) {
            return;
        }

        List<String> tagNames = List.of("붉은기", "부기", "통증", "건조함");
        for (String name : tagNames) {
            statusTagRepository.save(StatusTag.builder().name(name).build());
        }
    }
}