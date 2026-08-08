package likelion.madi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.CareRecordCreateForm;
import likelion.madi.dto.response.CareRecordResponse;
import likelion.madi.service.CareRecordService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "현재 상태 기록 페이지")
@RequestMapping("/api/v1/now")
public class CareRecordController {

    private final CareRecordService careRecordService;

    @Operation(summary = "현재 상태 기록 등록")
    @PostMapping(value = "/care-cards/{cardId}/records", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CareRecordResponse>> createCareRecord(
            @PathVariable Long cardId,
            @ModelAttribute CareRecordCreateForm form
    ) {
        CareRecordResponse result = careRecordService.create(
                cardId, form.getPhoto(), form.getStatusDescription(), form.getTags());
        return ResponseEntity.status(201).body(
                ApiResponse.success(
                        SuccessStatus.CARE_RECORD_CREATE_SUCCESS.getStatusCode(),
                        SuccessStatus.CARE_RECORD_CREATE_SUCCESS.getMessage(),
                        result
                )
        );
    }
}