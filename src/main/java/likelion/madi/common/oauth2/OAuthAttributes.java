package likelion.madi.common.oauth2;

import likelion.madi.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Slf4j
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String profileImage;
    private SocialProvider socialProvider;
    private String socialId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
                            String profileImage, SocialProvider socialProvider, String socialId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                      Map<String, Object> attributes) {
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        if (provider == SocialProvider.KAKAO) {
            return ofKakao(provider, userNameAttributeName, attributes);
        }
        return ofGoogle(provider, userNameAttributeName, attributes);
    }


    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(SocialProvider provider, String userNameAttributeName,
                                            Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount == null
                ? null
                : (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name(profile == null ? null : (String) profile.get("nickname"))
                .email(kakaoAccount == null ? null : (String) kakaoAccount.get("email"))
                .profileImage(profile == null ? null : (String) profile.get("profile_image_url"))
                .attributes(attributes)
                .socialProvider(provider)
                .socialId(String.valueOf(attributes.get(userNameAttributeName)))
                .nameAttributeKey(userNameAttributeName)
                .build();
    }