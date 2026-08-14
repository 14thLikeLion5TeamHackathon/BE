package likelion.madi.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {
    /// 200 OK
    AUTH_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    OAUTH2_LOGIN_SUCCESS(HttpStatus.OK, "소셜 로그인 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "액세스/리프레시 토큰 재발급 성공"),
    ONBOARDING_SUCCESS(HttpStatus.OK, "회원정보 등록(온보딩) 성공"),

    CARE_CARD_LIST_GET_SUCCESS(HttpStatus.OK, "케어카드 목록 조회 성공"),
    CALENDAR_EVENTS_GET_SUCCESS(HttpStatus.OK, "캘린더 일정 조회 성공"),
    CHECKLIST_GET_SUCCESS(HttpStatus.OK, "오늘의 케어 체크리스트 조회 성공"),
    CHECKLIST_UPDATE_SUCCESS(HttpStatus.OK, "체크리스트 완료 상태 변경 성공"),
    SCHEDULE_UPDATE_SUCCESS(HttpStatus.OK, "일정 수정 성공"),

    USER_INFO_GET_SUCCESS(HttpStatus.OK, "개인정보 조회 성공"),
    USER_INFO_UPDATE_SUCCESS(HttpStatus.OK, "개인정보 수정 성공"),
    CALENDAR_CONNECT_SUCCESS(HttpStatus.OK, "구글 캘린더 연동 성공"),
    KAKAO_NOTIFICATION_CONNECT_SUCCESS(HttpStatus.OK, "카카오 알림 연동 및 동의 설정 성공"),
    MY_CARE_CARD_LIST_GET_SUCCESS(HttpStatus.OK, "내 케어카드 조회 성공"),
    CARE_RECORD_LIST_GET_SUCCESS(HttpStatus.OK, "케어기록 리스트 조회 성공"),

    TREATMENT_SEARCH_SUCCESS(HttpStatus.OK, "시술명 검색 성공"),

    CARE_CARD_DETAIL_GET_SUCCESS(HttpStatus.OK, "케어카드 상세 조회 성공"),
    CARE_CARD_RECORDS_GET_SUCCESS(HttpStatus.OK, "카드별 이전 기록 조회 성공"),
    STORE_LIST_GET_SUCCESS(HttpStatus.OK, "AAC 매장 목록 조회 성공"),
    STORE_DETAIL_GET_SUCCESS(HttpStatus.OK, "AAC 매장 상세 조회 성공"),

    STATUS_TAG_LIST_GET_SUCCESS(HttpStatus.OK, "상태 태그 목록 조회 성공"),

    AI_FEEDBACK_GET_SUCCESS(HttpStatus.OK, "AI 피드백 조회 성공"),
    RECOVERY_TIMELINE_GET_SUCCESS(HttpStatus.OK, "회복 타임라인 조회 성공"),

    KAKAO_MESSAGE_SEND_SUCCESS(HttpStatus.OK, "카카오 알림 메시지 발송 성공"),
    BRIEFING_GET_SUCCESS(HttpStatus.OK, "관리 행동 브리핑 조회 성공"),

    LOCATION_UPDATE_SUCCESS(HttpStatus.OK, "위치 정보 저장 성공"),

    /// 201 CREATED
    AUTH_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    SCHEDULE_CREATE_SUCCESS(HttpStatus.CREATED, "일정 등록 성공"),
    CARE_CARD_CREATE_SUCCESS(HttpStatus.CREATED, "케어카드 생성 성공"),
    CARE_RECORD_CREATE_SUCCESS(HttpStatus.CREATED, "현재 상태 기록 등록 성공"),
    AI_FEEDBACK_CREATE_SUCCESS(HttpStatus.CREATED, "AI 피드백 생성 성공"),

    /// 204 NO CONTENT
    SCHEDULE_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "일정 삭제 성공"),
    CALENDAR_DISCONNECT_SUCCESS(HttpStatus.NO_CONTENT, "구글 캘린더 연동 해제 성공"),
    KAKAO_NOTIFICATION_DISCONNECT_SUCCESS(HttpStatus.NO_CONTENT, "카카오 알림 연동 해제 성공"),
    SOCIAL_UNLINK_SUCCESS(HttpStatus.NO_CONTENT, "소셜 로그인 연동 해제 성공"),
    MEMBER_WITHDRAW_SUCCESS(HttpStatus.NO_CONTENT, "회원탈퇴 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
