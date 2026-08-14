package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import likelion.madi.dto.request.UserUpdateRequest;
import likelion.madi.dto.response.UserIdResponse;
import likelion.madi.dto.response.UserInfoResponse;
import likelion.madi.repository.GoogleCalendarConnectionRepository;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final GoogleCalendarConnectionRepository googleCalendarConnectionRepository;
    private final KakaoNotificationRepository kakaoNotificationRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserIdResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        user.updatePartial(request.getName(), request.getBirthDate(), request.getGender());

        return UserIdResponse.builder()
                .userId(user.getUserId())
                .build();
    }

    @Transactional
    public void disconnectKakaoNotification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        KakaoNotification notification = kakaoNotificationRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_KAKAO_NOTIFICATION));

        kakaoNotificationRepository.delete(notification);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        kakaoNotificationRepository.findByUser(user).ifPresent(notification -> {
            try {
                kakaoOAuthClient.unlink(notification.getAccessToken());
            } catch (Exception e) {
                log.warn("카카오 연결 해제 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        });

        googleCalendarConnectionRepository.findByUser(user).ifPresent(connection -> {
            try {
                googleOAuthClient.revoke(connection.getAccessToken());
            } catch (Exception e) {
                log.warn("구글 연결 해제 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        });

        userRepository.delete(user);
    }
}