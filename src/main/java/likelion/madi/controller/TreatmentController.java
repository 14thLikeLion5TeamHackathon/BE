package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.PageResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.response.TreatmentResponse;
import likelion.madi.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "카드 생성 페이지")
@RequestMapping("/api/v1/create")
public class TreatmentController {

    private final TreatmentService treatmentService;

    @Operation(summary = "시술명 검색 (필터+검색 통합, 페이징)")
    @GetMapping("/treatments")
    public ResponseEntity<ApiResponse<PageResponse<TreatmentResponse>>> searchTreatments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        PageResponse<TreatmentResponse> result = treatmentService.search(keyword, category, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.TREATMENT_SEARCH_SUCCESS.getStatusCode(),
                        SuccessStatus.TREATMENT_SEARCH_SUCCESS.getMessage(),
                        result
                )
        );
    }
}
