package likelion.madi.service;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleTokenResponse;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final GoogleCalendarConnectionRepository calendarRepository;
    private final GoogleOAuthClient googleOAuthClient;

    @Transactional
    public void connectGoogleCalendar(Long userId, String authCode, String redirectUri) {
        // 1. 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. GoogleOAuthClient의 getTokens 메서드를 호출하여 구글 토큰 받아오기!
        GoogleTokenResponse tokenResponse = googleOAuthClient.getTokens(authCode, redirectUri);

        // 🔍 [터미널 진단 로그] 구글이 실제로 토큰을 잘 줬는지 확인
        System.out.println("==================================================");
        System.out.println("🔑 [CalendarService] 구글 응답 토큰 디버깅");
        System.out.println("Access Token  : " + tokenResponse.getAccessToken());
        System.out.println("Refresh Token : " + tokenResponse.getRefreshToken());
        System.out.println("==================================================");

        // 3. 기존 연동 이력이 있다면 갱신(Update), 없으면 신규 생성(Save)
        GoogleCalendarConnection connection = calendarRepository.findByUser(user)
                .map(existing -> {
                    existing.updateTokens(
                            tokenResponse.getAccessToken(),
                            tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken() : existing.getRefreshToken()
                    );
                    return existing;
                })
                .orElseGet(() -> GoogleCalendarConnection.builder()
                        .user(user)
                        .accessToken(tokenResponse.getAccessToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .build());

        calendarRepository.save(connection);
        log.info("User {} 의 구글 캘린더 연동이 완료되었습니다.", userId);
    }
}