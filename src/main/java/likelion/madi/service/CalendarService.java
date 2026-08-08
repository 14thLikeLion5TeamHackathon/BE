package likelion.madi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final GoogleOAuthClient googleOAuthClient; // 구글 API 통신용 클라이언트
    private final GoogleCalendarConnectionRepository connectionRepository;

    @Transactional
    public void connectGoogleCalendar(Long userId, String authCode) {

        // 1. 구글 서버에 authCode를 보내 Access/Refresh Token을 발급받음
        GoogleTokenResponse tokenResponse = googleOAuthClient.getTokens(authCode);

        // 2. 권한 검증: 사용자가 캘린더 접근 권한을 허용했는지 확인
        if (tokenResponse.getScope() == null || !tokenResponse.getScope().contains("calendar")) {
            throw new CustomException(ErrorCode.CALENDAR_SCOPE_MISSING);
        }

        // 3. DB (GOOGLE_CALENDAR_CONNECTION) 테이블에 토큰 및 연동 상태(CONNECTED) 저장
        GoogleCalendarConnection connection = GoogleCalendarConnection.builder()
                .userId(userId)
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .status("CONNECTED")
                .build();

        connectionRepository.save(connection);
    }
}