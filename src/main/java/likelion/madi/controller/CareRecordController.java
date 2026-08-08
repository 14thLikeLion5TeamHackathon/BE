package likelion.madi.controller;

import likelion.madi.domain.StatusTag;
import likelion.madi.dto.response.StatusTagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.CareRecordCreateForm;
import likelion.madi.dto.response.CareRecordResponse;
import likelion.madi.service.CareRecordService;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    @Operation(summary = "상태 태그 목록 조회")
    @GetMapping("/status-tags")
    public ResponseEntity<ApiResponse<List<StatusTagResponse>>> getStatusTags() {
        List<StatusTagResponse> result = careRecordService.getStatusTags();
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.STATUS_TAG_LIST_GET_SUCCESS.getStatusCode(),
                SuccessStatus.STATUS_TAG_LIST_GET_SUCCESS.getMessage(),
                result
        ));
    }
}