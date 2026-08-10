package likelion.madi.service;

import likelion.madi.dto.response.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final GoogleOAuthClient googleOAuthClient;
    // (기존에 작성하신 UserRepository, CalendarConnectionRepository 등 유지)

    @Transactional
    // 🌟 파라미터 맨 끝에 String redirectUri 가 추가되었습니다!
    public void connectGoogleCalendar(Long userId, String authCode, String redirectUri) {

        // 1. 유저 조회 (기존 코드 유지)
        // User user = userRepository.findById(userId)...

        // 🌟 2. 클라이언트에게 토큰을 요청할 때 redirectUri도 같이 넘겨줍니다!
        GoogleTokenResponse tokenResponse = googleOAuthClient.getTokens(authCode, redirectUri);

        // 3. 토큰에서 필요한 정보 추출 및 DB 저장 (기존 코드 유지)
        // ...
    }
}