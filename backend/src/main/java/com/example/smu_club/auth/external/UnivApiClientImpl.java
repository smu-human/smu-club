package com.example.smu_club.auth.external;


import com.example.smu_club.auth.dto.UnivAuthRequest;
import com.example.smu_club.auth.dto.UnivUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnivApiClientImpl implements UnivApiClient {

    private final RestClient restClient;

    @Override
    public UnivUserInfoResponse authenticate(String studentId, String password) {

        UnivAuthRequest requestPayload = new UnivAuthRequest(studentId, password);

        try {
            return restClient.post()
                    .uri("/auth")
                    .body(requestPayload)
                    .retrieve()
                    .body(UnivUserInfoResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("학교 인증 실패, 학번: {}, 상태코드: {}", studentId, e.getStatusCode());

            return null;
        }
    }
}
