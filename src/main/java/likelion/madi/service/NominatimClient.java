package likelion.madi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NominatimClient {

    private final RestTemplate restTemplate;

    public Map<String, Object> getAddress(double lat, double lon) {
        String url = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/reverse")
                .queryParam("format", "json")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("accept-language", "ko")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Madi-Hackathon/1.0");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        return (Map<String, Object>) response.getBody().get("address");
    }
}