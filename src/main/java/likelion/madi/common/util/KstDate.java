package likelion.madi.common.util;

import java.time.LocalDate;
import java.time.ZoneId;

// 사용자가 직접 입력하지 않을 시 저절로 LocalDate.now()를 호출해서 하루 밀려나옴 방지. (UTC 기준 방지)
public final class KstDate {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private KstDate() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}