package likelion.madi.controller;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.ChecklistUpdateRequest;
import likelion.madi.dto.response.TodayChecklistResponse;
import likelion.madi.service.TodayChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "오늘 페이지")
@RequestMapping("/api/v1/today")
public class TodayController {

    private final TodayChecklistService todayChecklistService;

    @Operation(summary = "오늘의 체크리스트 조회", description = "date를 안 보내면 오늘 날짜 기준으로 조회합니다.")
    @GetMapping("/checklist")
    public ResponseEntity<ApiResponse<TodayChecklistResponse>> getTodayChecklist(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TodayChecklistResponse result = todayChecklistService.getTodayChecklist(
                userId, date != null ? date : LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CHECKLIST_GET_SUCCESS.getStatusCode(),
                SuccessStatus.CHECKLIST_GET_SUCCESS.getMessage(),
                result
        ));
    }

    @Operation(summary = "체크리스트 완료 상태 변경")
    @PatchMapping("/checklist/{checklistId}")
    public ResponseEntity<ApiResponse<TodayChecklistResponse.Item>> updateChecklist(
            Authentication authentication,
            @PathVariable Long checklistId,
            @Valid @RequestBody ChecklistUpdateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TodayChecklistResponse.Item result = todayChecklistService.updateChecklist(
                userId, checklistId, request.getCompleted());
        return ResponseEntity.ok(ApiResponse.success(
                SuccessStatus.CHECKLIST_UPDATE_SUCCESS.getStatusCode(),
                SuccessStatus.CHECKLIST_UPDATE_SUCCESS.getMessage(),
                result
        ));
    }
}
