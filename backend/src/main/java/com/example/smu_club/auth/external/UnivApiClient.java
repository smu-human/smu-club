package com.example.smu_club.auth.external;

import com.example.smu_club.auth.dto.UnivUserInfoResponse;
import reactor.core.publisher.Mono;

public interface UnivApiClient {

    UnivUserInfoResponse authenticate(String studentId, String password);
}
