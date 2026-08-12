package likelion.madi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoMessageClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendMessage(String accessToken, String text, String link) {
        sendMessage(accessToken, text, link, "내 상태 기록하기");
    }

    public void sendMessage(String accessToken, String text, String link, String buttonTitle) {
        String sendUrl = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, Object> templateObject = new HashMap<>();
        templateObject.put("object_type", "text");
        templateObject.put("text", text);
        templateObject.put("link", Map.of(
                "web_url", link,
                "mobile_web_url", link
        ));
        if (buttonTitle != null) {
            templateObject.put("button_title", buttonTitle);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("template_object", writeAsJson(templateObject));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        restTemplate.postForEntity(sendUrl, request, String.class);
    }

    private String writeAsJson(Map<String, Object> templateObject) {
        try {
            return objectMapper.writeValueAsString(templateObject);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("카카오 메시지 template_object 변환에 실패했습니다.", e);
        }
    }
}
