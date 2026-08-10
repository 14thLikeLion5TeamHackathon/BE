package likelion.madi.service;

import likelion.madi.dto.response.TreatmentResponse;
import likelion.madi.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    // 시술명 검색 API (키워드+카테고리 필터 통합)
    @Transactional(readOnly = true)
    public List<TreatmentResponse> search(String keyword, String category) {
        return treatmentRepository.search(keyword, category)
                .stream()
                .map(TreatmentResponse::from)
                .toList();
    }
}
