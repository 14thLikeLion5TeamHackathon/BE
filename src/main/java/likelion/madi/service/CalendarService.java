package likelion.madi.service;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.dto.response.GoogleTokenResponse;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final GoogleCalendarConnectionRepository calendarRepository;
    private final GoogleOAuthClient googleOAuthClient;
    private final RestTemplate restTemplate = new RestTemplate(); // 👈 구글 이메일 조회용

    @Transactional
    public void connectGoogleCalendar(Long userId, String authCode, String redirectUri) {
        // 1. 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. GoogleOAuthClient를 통해 구글 토큰 받아오기
        GoogleTokenResponse tokenResponse = googleOAuthClient.getTokens(authCode, redirectUri);

        // 🔍 [터미널 진단 로그]
        System.out.println("==================================================");
        System.out.println("🔑 [CalendarService] 구글 응답 토큰 디버깅");
        System.out.println("Access Token  : " + tokenResponse.getAccessToken());
        System.out.println("Refresh Token : " + tokenResponse.getRefreshToken());
        System.out.println("==================================================");

        // 3. 🌟 [신규 추가] Access Token으로 구글 계정 이메일 조회하기
        String googleEmail = fetchGoogleEmail(tokenResponse.getAccessToken());

        // 4. 기존 연동 이력이 있다면 갱신(reconnect), 없으면 신규 생성(Save)
        GoogleCalendarConnection connection = calendarRepository.findByUser(user)
                .map(existing -> {
                    // 🌟 엔티티에 이미 만들어져 있는 reconnect() 메서드 사용!
                    existing.reconnect(
                            googleEmail,
                            tokenResponse.getAccessToken(),
                            tokenResponse.getRefreshToken() != null ? tokenResponse.getRefreshToken() : existing.getRefreshToken()
                    );
                    return existing;
                })
                .orElseGet(() -> GoogleCalendarConnection.builder()
                        .user(user)
                        .accessToken(tokenResponse.getAccessToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .googleEmail(googleEmail) // 👈 조회해온 구글 이메일 저장
                        // 엔티티의 @Builder 생성자에서 status와 connectedAt은 자동으로 채워짐
                        .build());

        calendarRepository.save(connection);
        log.info("User {} 의 구글 캘린더 연동이 완료되었습니다. (Email: {})", userId, googleEmail);
    }

    /**
     * 🌟 구글 Access Token을 사용하여 구글 계정 이메일을 조회하는 메서드
     */
    private String fetchGoogleEmail(String accessToken) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken); // Bearer 토큰 세팅

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("email")) {
                return (String) response.getBody().get("email");
            }
        } catch (Exception e) {
            log.warn("구글 유저 이메일 조회 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }
}