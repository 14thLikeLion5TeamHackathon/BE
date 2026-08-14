package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.response.CareCardDetailResponse;
import likelion.madi.dto.response.CareCardListResponse;
import likelion.madi.dto.response.CareRecordTimelineResponse;
import likelion.madi.service.CareCardService;
import likelion.madi.service.CareRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "카드 상세 페이지")
@RequestMapping("/api/v1/cards")
public class CareCardDetailController {

    private final CareCardService careCardService;
    private final CareRecordService careRecordService;

    @Operation(summary = "케어카드 상세 조회")
    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CareCardDetailResponse>> getCareCardDetail(
            Authentication authentication,
            @PathVariable Long cardId,
            @RequestParam("city") String city,
            @RequestParam("district") String district
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CareCardDetailResponse result = careCardService.getDetail(userId, cardId, city, district);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CARE_CARD_DETAIL_GET_SUCCESS.getStatusCode(),
                SuccessStatus.CARE_CARD_DETAIL_GET_SUCCESS.getMessage(),
                result
        ));
    }

    @Operation(summary = "카드별 이전 기록(회복 타임라인) 조회")
    @GetMapping("/{cardId}/records")
    public ResponseEntity<ApiResponse<CareRecordTimelineResponse>> getCareCardRecords(
            Authentication authentication,
            @PathVariable Long cardId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CareRecordTimelineResponse result = careRecordService.getTimeline(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CARE_CARD_RECORDS_GET_SUCCESS.getStatusCode(),
                SuccessStatus.CARE_CARD_RECORDS_GET_SUCCESS.getMessage(),
                result
        ));
    }

    @Operation(summary = "케어카드 목록 조회 (카드별 최근 기록 포함)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CareCardListResponse>>> getMyCareCards(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        List<CareCardListResponse> result = careCardService.getMyCareCards(userId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CARE_CARD_LIST_GET_SUCCESS.getStatusCode(),
                SuccessStatus.CARE_CARD_LIST_GET_SUCCESS.getMessage(),
                result
        ));
    }
}