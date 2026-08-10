package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.response.CareCardDetailResponse;
import likelion.madi.service.CareCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "카드 상세 페이지")
@RequestMapping("/api/v1/cards")
public class CareCardDetailController {

    private final CareCardService careCardService;

    @Operation(summary = "케어카드 상세 조회")
    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CareCardDetailResponse>> getCareCardDetail(
            Authentication authentication,
            @PathVariable Long cardId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CareCardDetailResponse result = careCardService.getDetail(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CARE_CARD_DETAIL_GET_SUCCESS.getStatusCode(),
                SuccessStatus.CARE_CARD_DETAIL_GET_SUCCESS.getMessage(),
                result
        ));
    }
}