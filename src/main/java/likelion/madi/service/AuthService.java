package likelion.madi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.JwtException;
import likelion.madi.common.exception.UnauthorizedException;
import likelion.madi.common.jwt.JwtService;
import likelion.madi.common.response.ErrorStatus;
import likelion.madi.domain.User;
import likelion.madi.dto.response.TokenRefreshResponse;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 3600L;

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public TokenRefreshResponse refresh(String refreshToken) {
        Long userId;
        try {
            userId = jwtService.extractUserId(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_REFRESH_TOKEN));

        // 저장된 리프레시 토큰과 일치하는지 대조 (탈취/로그아웃된 토큰 재사용 방지)
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtService.createAccessToken(user);
        String newRefreshToken = jwtService.createRefreshToken(userId);

        user.updateRefreshToken(newRefreshToken); // 로테이션: 재사용 방지 위해 매번 갱신

        return TokenRefreshResponse.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(ACCESS_TOKEN_VALIDITY_SECONDS)
                .build();
    }
}