package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.dto.response.GoogleCalendarEventsResponseDto;
import likelion.madi.service.GoogleCalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Today - Google Calendar", description = "구글 캘린더 연동 및 일정 조회 API")
@RestController
@RequestMapping("/api/v1/today/calendar")
@RequiredArgsConstructor
public class GoogleCalendarEventController {

    private final GoogleCalendarEventService googleCalendarEventService;

    @Operation(
            summary = "연동된 구글 캘린더 일정 조회",
            description = "로그인된 사용자의 구글 캘린더 연동 토큰을 이용해 구글 캘린더 일정을 조회합니다."
    )
    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getTodayCalendarEvents(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD)", example = "2026-08-14")
            @RequestParam(value = "startDate", required = false) String startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD)", example = "2026-08-14")
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        GoogleCalendarEventsResponseDto responseData = googleCalendarEventService.getCalendarEvents(userId, startDate, endDate);

        // 명세서 규격에 맞춘 응답 JSON 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "캘린더 일정 조회 성공");
        response.put("data", responseData);

        return ResponseEntity.ok(response);
    }
}