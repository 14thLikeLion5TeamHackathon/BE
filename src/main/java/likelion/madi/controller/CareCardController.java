package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.CareCardCreateRequest;
import likelion.madi.dto.response.CareCardCreateResponse;
import likelion.madi.service.CareCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "카드 생성 페이지")
@RequestMapping("/api/v1/create")
public class CareCardController {

    private final CareCardService careCardService;

    @Operation(summary = "케어카드 생성")
    @PostMapping("/care-cards")
    public ResponseEntity<ApiResponse<CareCardCreateResponse>> createCareCard(
            Authentication authentication,
            @Valid @RequestBody CareCardCreateRequest request
            ) {
        Long userId = (Long) authentication.getPrincipal();
        CareCardCreateResponse result = careCardService.create(userId, request);

        return ResponseEntity.status(201).body(ApiResponse.success(SuccessStatus.CARE_CARD_CREATE_SUCCESS.getStatusCode(),
                SuccessStatus.CARE_CARD_CREATE_SUCCESS.getMessage(), result));
    }
}
