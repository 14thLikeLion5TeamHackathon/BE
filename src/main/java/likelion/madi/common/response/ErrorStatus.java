package likelion.madi.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {
    /// 400 BAD REQUEST
    BAD_REQUEST_MISSING_PARAM(HttpStatus.BAD_REQUEST, "MISSING_PARAM", "요청 값이 올바르지 않습니다."),
    BAD_REQUEST_MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_FIELD", "필수 입력값이 누락되었습니다."),
    BAD_REQUEST_VALID_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "요청 값 검증에 실패했습니다."),
    BAD_REQUEST_INVALID_PHONE(HttpStatus.BAD_REQUEST, "INVALID_PHONE", "전화번호 형식이 올바르지 않습니다."),
    BAD_REQUEST_RECORD_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "RECORD_CONTENT_REQUIRED", "사진 또는 상태 설명 중 최소 하나는 입력해야 합니다."),
    BAD_REQUEST_INVALID_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME", "일정 날짜/시간 형식이 올바르지 않습니다."),
    BAD_REQUEST_NOT_SUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "NOT_SUPPORTED_MEDIA_TYPE", "지원하지 않는 미디어 타입입니다."),
    BAD_REQUEST_INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_SIZE", "이미지 파일 크기가 15MB 보다 큽니다."),
    BAD_REQUEST_TOO_MANY_PHOTOS(HttpStatus.BAD_REQUEST, "TOO_MANY_PHOTOS", "사진은 최대 5장까지 첨부할 수 있습니다."),
    BAD_REQUEST_INVALID_ONBOARDING_INPUT(HttpStatus.BAD_REQUEST, "INVALID_ONBOARDING_INPUT", "필수 입력 항목이 누락되었거나 필수 약관 동의가 필요합니다." ),
    BAD_REQUEST_SYMPTOM_TAGS_REQUIRED(HttpStatus.BAD_REQUEST, "SYMPTOM_TAGS_REQUIRED", "모든 증상 태그를 입력해야 합니다."),

    /// 401 UNAUTHORIZED
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_USER", "인증되지 않은 사용자입니다."),
    UNAUTHORIZED_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "EXPIRED_OR_INVALID_REFRESH_TOKEN", "만료된 토큰입니다."),
    UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_OR_INVALID_REFRESH_TOKEN", "유효하지 않은 토큰입니다."),
    UNAUTHORIZED_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_OR_INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    UNAUTHORIZED_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 일치하지 않습니다."),

    /// 403 FORBIDDEN
    FORBIDDEN_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN_RESOURCE_ACCESS", "접근 권한이 없습니다."),
    FORBIDDEN_SCHEDULE_NOT_EDITABLE(HttpStatus.FORBIDDEN, "SCHEDULE_NOT_EDITABLE", "캘린더 연동으로 가져온 일정은 수정하거나 삭제할 수 없습니다."),

    /// 404 NOT FOUND
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_CARE_CARD(HttpStatus.NOT_FOUND, "CARE_CARD_NOT_FOUND", "해당 케어카드를 찾을 수 없습니다."),
    NOT_FOUND_CARE_RECORD(HttpStatus.NOT_FOUND, "CARE_RECORD_NOT_FOUND", "해당 케어기록을 찾을 수 없습니다."),
    NOT_FOUND_TREATMENT(HttpStatus.NOT_FOUND, "TREATMENT_NOT_FOUND", "해당 시술 정보를 찾을 수 없습니다."),
    NOT_FOUND_SCHEDULE(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "해당 일정을 찾을 수 없습니다."),
    NOT_FOUND_STORE(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "해당 매장을 찾을 수 없습니다."),
    NOT_FOUND_CHECKLIST(HttpStatus.NOT_FOUND, "CHECKLIST_NOT_FOUND", "해당 체크리스트 항목을 찾을 수 없습니다."),
    NOT_FOUND_RECOVERY_GUIDE(HttpStatus.NOT_FOUND, "RECOVERY_GUIDE_NOT_FOUND", "해당 회복 가이드를 찾을 수 없습니다."),
    NOT_FOUND_AI_FEEDBACK(HttpStatus.NOT_FOUND, "AI_FEEDBACK_NOT_FOUND", "해당 AI 피드백을 찾을 수 없습니다."),
    NOT_FOUND_CALENDAR_CONNECTION(HttpStatus.NOT_FOUND, "CALENDAR_CONNECTION_NOT_FOUND", "연동된 구글 캘린더 정보를 찾을 수 없습니다."),
    NOT_FOUND_KAKAO_NOTIFICATION(HttpStatus.NOT_FOUND, "KAKAO_NOTIFICATION_NOT_FOUND", "연동된 카카오 알림 정보를 찾을 수 없습니다."),
    NOT_FOUND_SOCIAL_ACCOUNT(HttpStatus.NOT_FOUND, "SOCIAL_ACCOUNT_NOT_FOUND", "연동된 소셜 계정을 찾을 수 없습니다."),

    /// 409 CONFLICT
    CONFLICT_DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다."),
    CONFLICT_LAST_LOGIN_METHOD(HttpStatus.CONFLICT, "LAST_LOGIN_METHOD", "마지막으로 남은 로그인 수단은 연동 해제할 수 없습니다."),
    CONFLICT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "ALREADY_CONNECTED", "이미 연동되어 있습니다."),
    CONFLICT_ALREADY_ONBOARDED_USER(HttpStatus.CONFLICT, "ALREADY_ONBOARDED_USER", "이미 온보딩 정보 등록이 완료된 사용자입니다."),
    CONFLICT_FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "FEEDBACK_ALREADY_EXISTS", "해당 기록에 대한 AI 피드백이 이미 존재합니다."),

    /// 415 UNSUPPORTED MEDIA TYPE
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 Content-Type 입니다."),

    /// 429 TOO MANY REQUESTS
    TOO_MANY_REQUESTS_FEEDBACK_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "FEEDBACK_QUOTA_EXCEEDED", "오늘 분석 횟수를 모두 사용했어요. (일 3회 제한)"),

    /// 500 SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_API_ERROR", "외부 API(구글 캘린더·카카오·기상) 연동 중 오류가 발생했습니다."),

    /// 503 SERVICE UNAVAILABLE
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "서버에 연결할 수 없습니다."),

    /// 504 GATEWAY TIMEOUT
    GATEWAY_TIMEOUT_AI_ANALYSIS(HttpStatus.GATEWAY_TIMEOUT, "AI_ANALYSIS_TIMEOUT", "AI 분석 서버의 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),

    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}