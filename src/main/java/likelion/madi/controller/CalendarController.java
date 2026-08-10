package likelion.madi.controller;

import likelion.madi.service.CalendarService; // 패키지 경로에 맞게 확인
import likelion.madi.dto.request.CalendarConnectRequest; //
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar", description = "구글 캘린더 연동 API")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "구글 캘린더 연동", description = "구글 인증 코드를 받아 캘린더 연동 토큰을 발급받고 저장합니다.")
    @PostMapping("/connect")
    public ResponseEntity<Void> connectCalendar(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CalendarConnectRequest request) {

        // 🌟 request.getRedirectUri() 를 서비스로 같이 넘겨줍니다!
        calendarService.connectGoogleCalendar(userId, request.getAuthCode(), request.getRedirectUri());

        return ResponseEntity.ok().build();
    }
}