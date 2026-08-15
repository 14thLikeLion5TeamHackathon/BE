package likelion.madi.controller; // 본인 패키지 경로에 맞게 수정

import likelion.madi.dto.response.CalendarDisconnectResponseDto;
import likelion.madi.service.GoogleCalendarDisconnectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
@Tag(name = "Google Calendar", description = "구글 캘린더 연동 및 해제 API")
public class GoogleCalendarDisconnectController {

    private final GoogleCalendarDisconnectService disconnectService;

    @Operation(summary = "구글 캘린더 연동 해제", description = "사용자의 계정과 연결된 구글 캘린더 연동 상태를 해제합니다.")
    @DeleteMapping("/connect")
    public ResponseEntity<?> disconnectCalendar(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        try {
            // 1. 서비스 로직 호출 (해제 처리)
            disconnectService.disconnectGoogleCalendar(userId);

            // 2. 성공 응답 반환
            CalendarDisconnectResponseDto response = CalendarDisconnectResponseDto.builder()
                    .success(true)
                    .code(HttpStatus.OK.value())
                    .message("구글 캘린더 연동 해제 성공")
                    .data(null)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 🚨 [예외 처리 1] 유저가 없거나, 연동 정보가 없거나, 이미 해제된 경우 (400 Bad Request)
            log.warn("캘린더 연동 해제 실패: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("error_code", "CALENDAR_NOT_CONNECTED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            // 🚨 [예외 처리 2] 서버 내부 에러 (500 Internal Server Error)
            log.error("캘린더 연동 해제 중 서버 에러 발생: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("error_code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류로 인해 캘린더 연동 해제에 실패했습니다.");
            errorResponse.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}