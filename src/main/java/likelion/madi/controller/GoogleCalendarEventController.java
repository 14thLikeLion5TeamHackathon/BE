package likelion.madi.controller;

import likelion.madi.dto.response.GoogleCalendarEventsResponseDto;
import likelion.madi.service.GoogleCalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/today/calendar")
@RequiredArgsConstructor
public class GoogleCalendarEventController {

    private final GoogleCalendarEventService googleCalendarEventService;

    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getTodayCalendarEvents(
            @RequestAttribute(value = "userId", required = false) Long userId, // JWT 토큰에서 추출한 유저 PK
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        // 테스트/개발 중 userId가 null일 경우 임시 ID(1L) 적용
        Long targetUserId = (userId != null) ? userId : 1L;

        GoogleCalendarEventsResponseDto responseData = googleCalendarEventService.getCalendarEvents(targetUserId, startDate, endDate);

        // 팀 공통 응답 규격(success, code, message, data) 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "캘린더 일정 조회 성공");
        response.put("data", responseData);

        return ResponseEntity.ok(response);
    }
}