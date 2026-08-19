package likelion.madi.service;

import likelion.madi.common.response.PageResponse;
import likelion.madi.dto.response.TreatmentResponse;
import likelion.madi.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public PageResponse<TreatmentResponse> search(String keyword, String category, int page, int size) {
        Page<TreatmentResponse> result = treatmentRepository
                .search(keyword, category, PageRequest.of(page, size))
                .map(TreatmentResponse::from);
        return PageResponse.from(result);
    }
}
