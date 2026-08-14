package likelion.madi.service; // 본인 패키지 경로에 맞게 수정

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository; // 유저 조회를 위한 레포지토리 추가
import likelion.madi.enums.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarDisconnectService {

    private final GoogleCalendarConnectionRepository connectionRepository;
    private final UserRepository userRepository; // 👈 추가된 부분

    @Transactional
    public void disconnectGoogleCalendar(Long userId) {
        log.info("유저 ID [{}]의 구글 캘린더 연동 해제를 시작합니다.", userId);

        // 1. userId로 User 엔티티를 먼저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2. User 객체를 사용하여 캘린더 연동 정보 조회
        GoogleCalendarConnection connection = connectionRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("연동된 구글 캘린더 정보가 없습니다."));

        // 3. 이미 해제되어 있는 상태인지 검증
        if (connection.getStatus() == ConnectionStatus.DISCONNECTED) {
            throw new IllegalArgumentException("이미 캘린더 연동이 해제되어 있습니다.");
        }

        // 4. 엔티티에 구현된 disconnect() 메서드 호출 (토큰 null 처리 및 상태 변경)
        connection.disconnect();

        log.info("유저 ID [{}]의 구글 캘린더 연동이 성공적으로 해제되었습니다.", userId);
    }
}