package com.example.smu_club.auth.controller;


import com.example.smu_club.auth.dto.JwtTokenResponse;
import com.example.smu_club.auth.dto.LoginRequest;
import com.example.smu_club.auth.dto.ReissueRequest;
import com.example.smu_club.auth.dto.SignupRequest;
import com.example.smu_club.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("사용자 로그인 시도 :{}", loginRequest.getStudentId());

        try {
            JwtTokenResponse jwtTokenResponse = authService.login(loginRequest);
            return ResponseEntity.ok(jwtTokenResponse);
        } catch (LoginException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtTokenResponse> reissue(@RequestBody ReissueRequest reissueRequest) {
        try {
            // 성공했을 경우의 로직
            JwtTokenResponse jwtTokenResponse = authService.reissueTokens(reissueRequest);
            return ResponseEntity.ok(jwtTokenResponse);

        } catch (LoginException e) {
            log.warn("refresh Token 검증 실패");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @PostMapping("signup")
    public ResponseEntity<JwtTokenResponse> signup(@RequestBody SignupRequest signupRequest) {
        log.info("신규 사용자 회원가입 시도 :{}", signupRequest.getStudentId());
        try {
            JwtTokenResponse jwtTokenResponse = authService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(jwtTokenResponse);
        } catch (LoginException e) {
            // 학교 API 인증 실패
            log.warn("학교 API 인증 실패 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            // 이미 가입된 회원인 경우
            log.warn("이미 가입된 사용자: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}
