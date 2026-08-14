package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import likelion.madi.dto.request.UserUpdateRequest;
import likelion.madi.dto.request.UserLocationUpdateRequest;
import likelion.madi.dto.response.UserIdResponse;
import likelion.madi.dto.response.UserInfoResponse;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
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
    public void updateLocation(Long userId, UserLocationUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        // TODO: 지오코딩 서비스 정해지면 여기서 위경도 → city/district 변환
        String city = null;
        String district = null;

        user.updateLocation(request.getLatitude(), request.getLongitude(), city, district);
    }
}