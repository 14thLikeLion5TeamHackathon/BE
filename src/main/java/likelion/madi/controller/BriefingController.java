package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.domain.User;
import likelion.madi.dto.response.BriefingResponse;
import likelion.madi.repository.UserRepository;
import likelion.madi.service.BriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "오늘 페이지")
@RequestMapping("/api/v1/today")
public class BriefingController {

    private final BriefingService briefingService;
    private final UserRepository userRepository;

    @Operation(summary = "관리 행동 브리핑(행동 문장+주의등급+근거)")
    @GetMapping("/briefing")
    public ResponseEntity<ApiResponse<BriefingResponse>> getBriefing(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("city") String city,
            @RequestParam("district") String district
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        BriefingResponse result = briefingService.getBriefing(user, date, city, district);

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.CALENDAR_EVENTS_GET_SUCCESS.getStatusCode(),
                        "관리 행동 브리핑 조회 성공",
                        result
                )
        );
    }
}
