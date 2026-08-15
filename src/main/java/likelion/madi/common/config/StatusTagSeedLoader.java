package likelion.madi.common.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import likelion.madi.domain.StatusTag;
import likelion.madi.repository.StatusTagRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatusTagSeedLoader implements CommandLineRunner {

    private final StatusTagRepository statusTagRepository;

    private static final List<TagSeed> TAG_SEEDS = List.of(
            new TagSeed("붉은기", "redness"),
            new TagSeed("부기", "swelling"),
            new TagSeed("통증", "pain"),
            new TagSeed("건조함", "dryness")
    );

    @Override
    public void run(String... args) {
        Map<String, StatusTag> existingByName = statusTagRepository.findAll().stream()
                .collect(Collectors.toMap(StatusTag::getName, tag -> tag));

        for (TagSeed seed : TAG_SEEDS) {
            StatusTag existing = existingByName.get(seed.name());
            if (existing == null) {
                statusTagRepository.save(StatusTag.builder().name(seed.name()).code(seed.code()).build());
            } else if (existing.getCode() == null) {
                existing.updateCode(seed.code());
                statusTagRepository.save(existing);
            }
        }
    }

    private record TagSeed(String name, String code) {
    }
}
