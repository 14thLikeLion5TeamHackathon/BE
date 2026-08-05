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

        if (provider == SocialProvider.NAVER) {
            return ofNaver(provider, userNameAttributeName, attributes);
        } else if (provider == SocialProvider.KAKAO) {
            return ofKakao(provider, userNameAttributeName, attributes);
        }
        return ofGoogle(provider, userNameAttributeName, attributes);
    }



    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(SocialProvider provider, String userNameAttributeName,
                                            Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .profileImage((String) response.get("profile_image"))
                .attributes(attributes)
                .socialProvider(provider)
                .socialId((String) response.get("id"))
                .nameAttributeKey(userNameAttributeName)
                .build();
    }


}
