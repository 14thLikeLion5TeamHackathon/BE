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

    // 시술명 검색 API
    @Transactional(readOnly = true)
    public List<TreatmentResponse> search(String keyword) {
        return treatmentRepository.findByNameContaining(keyword)
                .stream()
                .map(TreatmentResponse::from)
                .toList();
    }
}
