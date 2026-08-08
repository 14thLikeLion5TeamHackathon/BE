package likelion.madi.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "구글 캘린더 연동 API")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "구글 캘린더 연결 처리",
            description = "마이페이지에서 전달받은 구글 인증 코드를 통해 접근 권한을 획득하고 연동 상태를 저장합니다."
    )
    @PostMapping("/calendar/connect")
    public ResponseEntity<CommonResponse<Void>> connectCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CalendarConnectRequest request) {

        // 헤더의 JWT 토큰에서 추출한 userId와 프론트가 전달한 authCode 전달
        calendarService.connectGoogleCalendar(userDetails.getUserId(), request.getAuthCode());

        // 공통 응답 포맷으로 성공 반환
        return ResponseEntity.ok(
                CommonResponse.success(200, "구글 캘린더 연동이 완료되었습니다.", null)
        );
    }
}