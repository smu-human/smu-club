package com.example.smu_club.auth.external;


import com.example.smu_club.auth.dto.UnivUserInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class SlowMockUnivApiClient implements UnivApiClient {

    // @CircuitBreaker 어노테이션을 통해 규칙을 적용합니다.
    @CircuitBreaker(name = "univApi", fallbackMethod = "authenticateFallback")
    @Override
    public UnivUserInfoResponse authenticate(String studentId, String password) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new UnivUserInfoResponse(studentId, "차준규", studentId, "휴먼지능정보공학", "Mock 부전공");

    }


}
