package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.request.ScheduleCreateRequest;
import likelion.madi.dto.request.ScheduleUpdateRequest;
import likelion.madi.dto.response.ScheduleResponse;
import likelion.madi.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "오늘 페이지")
@RequestMapping("/api/v1/today")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "일정 직접 입력")
    @PostMapping("/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        ScheduleResponse result = scheduleService.create(userId, request);
        return ResponseEntity.status(SuccessStatus.SCHEDULE_CREATE_SUCCESS.getStatusCode()).body(
                ApiResponse.success(
                        SuccessStatus.SCHEDULE_CREATE_SUCCESS.getStatusCode(),
                        SuccessStatus.SCHEDULE_CREATE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        ScheduleResponse result = scheduleService.update(userId, scheduleId, request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.SCHEDULE_UPDATE_SUCCESS.getStatusCode(),
                        SuccessStatus.SCHEDULE_UPDATE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        scheduleService.delete(userId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}