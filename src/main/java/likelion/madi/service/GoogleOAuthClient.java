package likelion.madi.service;

import likelion.madi.dto.response.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient {

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // ❌ (삭제됨) 더 이상 application.yaml이나 하드코딩으로 redirectUri를 읽어오지 않습니다.

    // 🌟 파라미터로 String redirectUri를 직접 받습니다!
    public GoogleTokenResponse getTokens(String authCode, String redirectUri) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        // 🌟 이제 프론트가 보내준 동적 주소가 구글로 전달됩니다!
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 에러 원인 파악을 위한 로그 출력 (테스트 성공 후 지우셔도 됩니다)
        System.out.println("==================================================");
        System.out.println("🚀 [GoogleOAuthClient] 구글 토큰 요청 데이터 진단");
        System.out.println("1. authCode     = " + authCode);
        System.out.println("2. clientId     = " + clientId);
        System.out.println("3. redirectUri  = " + redirectUri);
        System.out.println("==================================================");

        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                GoogleTokenResponse.class
        );

        return response.getBody();
    }

    public void revoke(String accessToken) {
        String revokeUrl = "https://oauth2.googleapis.com/revoke";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        restTemplate.postForEntity(revokeUrl, request, String.class);
    }
}