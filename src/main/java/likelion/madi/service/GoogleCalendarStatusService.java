package likelion.madi.service;

import likelion.madi.domain.GoogleCalendarConnection;
import likelion.madi.domain.User;
import likelion.madi.dto.response.CalendarStatusResponseDto;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleCalendarStatusService {

    private final UserRepository userRepository;
    private final GoogleCalendarConnectionRepository connectionRepository;

    /**
     * 구글 캘린더 연동 상태 조회 비즈니스 로직
     */
    @Transactional(readOnly = true)
    public CalendarStatusResponseDto getStatus(Long userId) {
        // 1. 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        // 2. 캘린더 연동 이력 조회 (한 번도 없으면 null 반환 -> data: null)
        Optional<GoogleCalendarConnection> connectionOpt = connectionRepository.findByUser(user);
        if (connectionOpt.isEmpty()) {
            return null;
        }

        GoogleCalendarConnection connection = connectionOpt.get();

        // 3. 응답 DTO 매핑
        return CalendarStatusResponseDto.builder()
                .connectionId(connection.getConnectionId()) // 👈 getId() -> getConnectionId()로 수정
                .googleEmail(connection.getGoogleEmail())
                .status(connection.getStatus())
                .connectedAt(connection.getConnectedAt()) // 조회할 때마다 시간이 바뀌지 않고, DB에 저장된 연동 시각을 그대로 가져옵니다.
                .build();
    }
}