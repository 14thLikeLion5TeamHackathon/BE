package likelion.madi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.madi.common.response.ApiResponse;
import likelion.madi.common.response.SuccessStatus;
import likelion.madi.dto.response.AiFeedbackResponse;
import likelion.madi.service.AiFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "AI 피드백")
@RequestMapping("/api/v1/now/records/{recordId}/feedback")
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    @Operation(summary = "AI 피드백 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<AiFeedbackResponse>> createFeedback(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long recordId
    ) {
        AiFeedbackResponse result = aiFeedbackService.generate(userId, recordId);
        return ResponseEntity.status(201).body(
                ApiResponse.success(
                        SuccessStatus.AI_FEEDBACK_CREATE_SUCCESS.getStatusCode(),
                        SuccessStatus.AI_FEEDBACK_CREATE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @Operation(summary = "AI 피드백 단건 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<AiFeedbackResponse>> getFeedback(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long recordId
    ) {
        AiFeedbackResponse result = aiFeedbackService.getFeedback(userId, recordId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessStatus.AI_FEEDBACK_GET_SUCCESS.getStatusCode(),
                        SuccessStatus.AI_FEEDBACK_GET_SUCCESS.getMessage(),
                        result
                )
        );
    }
}