package likelion.madi.common.oauth2;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import likelion.madi.domain.SocialAccount;
import likelion.madi.domain.User;
import likelion.madi.repository.SocialAccountRepository;
import likelion.madi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        log.info("소셜 로그인 시도 - provider: {}, socialId: {}, name: {}",
                attributes.getSocialProvider(), attributes.getSocialId(), attributes.getName());

        User user = findOrCreateUser(attributes);

        log.info("소셜 로그인 완료 - userId: {}", user.getUserId());

        // 이후 JwtAuthenticationSuccessHandler 등에서 사용자를 다시 찾을 수 있도록
        // authority 값에 user_id를 담아 전달합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(String.valueOf(user.getUserId()))),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User findOrCreateUser(OAuthAttributes attributes) {
        return socialAccountRepository
                .findByProviderAndProviderUserId(attributes.getSocialProvider(), attributes.getSocialId())
                .map(SocialAccount::getUser)
                .orElseGet(() -> registerNewUser(attributes));
    }

    private User registerNewUser(OAuthAttributes attributes) {
        User user = userRepository.save(User.builder()
                .name(attributes.getName())
                .build());

        socialAccountRepository.save(SocialAccount.builder()
                .user(user)
                .provider(attributes.getSocialProvider())
                .providerUserId(attributes.getSocialId())
                .build());

        log.info("신규 유저 생성 - userId: {}, provider: {}", user.getUserId(), attributes.getSocialProvider());
        return user;
    }
}
