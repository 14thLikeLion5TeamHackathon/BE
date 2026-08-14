package likelion.madi.dto.response; // 패키지는 프로젝트에 맞게 수정

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalendarDisconnectResponseDto {
    private boolean success;
    private int code;
    private String message;
    private Object data; // 해제 성공 시 null을 반환
}