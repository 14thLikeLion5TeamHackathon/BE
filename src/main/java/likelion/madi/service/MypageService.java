package likelion.madi.service;

import likelion.madi.common.exception.NotFoundException;
import likelion.madi.common.jwt.JwtService;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.BlacklistedToken;
import likelion.madi.domain.KakaoNotification;
import likelion.madi.domain.User;
import likelion.madi.dto.request.UserUpdateRequest;
import likelion.madi.dto.response.UserIdResponse;
import likelion.madi.dto.response.UserInfoResponse;
import likelion.madi.repository.BlacklistedTokenRepository;
import likelion.madi.repository.KakaoNotificationRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final KakaoNotificationRepository kakaoNotificationRepository;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

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
    public void logout(Long userId, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER));

        String tokenId = jwtService.extractTokenId(accessToken);
        LocalDateTime expiresAt = jwtService.extractExpiration(accessToken)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .tokenId(tokenId)
                        .expiresAt(expiresAt)
                        .build()
        );

        user.updateRefreshToken(null);
    }
}