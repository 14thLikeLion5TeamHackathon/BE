package likelion.madi.common.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import likelion.madi.common.security.entity.SecurityMember;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 이메일/비밀번호 로그인이 없는 앱이라, username 자리에 user_id 문자열을 씁니다.
    // JwtAuthenticationProcessingFilter가 토큰의 subject(user_id)로 이 메서드를 호출하게 됩니다.
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(Long.parseLong(userId))
                .map(SecurityMember::new)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다 : " + userId));
    }
}
