package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.dto.response.CalendarStatusResponseDto;
import likelion.madi.service.GoogleCalendarStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar Status", description = "구글 캘린더 연동 상태 조회 API")
public class GoogleCalendarStatusController {

    private final GoogleCalendarStatusService googleCalendarStatusService;

    @Operation(summary = "구글 캘린더 연동 상태 조회", description = "유저의 현재 구글 캘린더 연동 상태(CONNECTED/DISCONNECTED/null)를 조회합니다.")
    @GetMapping("/connect")
    public ResponseEntity<Map<String, Object>> getCalendarStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        CalendarStatusResponseDto statusDto = googleCalendarStatusService.getStatus(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "");
        response.put("data", statusDto); // 미연동 시 data: null

        return ResponseEntity.ok(response);
    }
}