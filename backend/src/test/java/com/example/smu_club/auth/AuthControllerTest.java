package com.example.smu_club.auth;


import com.example.smu_club.auth.controller.AuthController;
import com.example.smu_club.auth.dto.JwtTokenResponse;
import com.example.smu_club.auth.dto.LoginRequest;
import com.example.smu_club.auth.service.AuthService;
import com.example.smu_club.auth.token.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("사용자 로그인 성공")
    void loginSuccess() throws Exception {

        // given
        // LoginRequest (객체) -
        LoginRequest loginRequest = new LoginRequest("202010823[학번]", "password");

        JwtTokenResponse expectedResponse = new JwtTokenResponse("Bearer", "fake-access-token", "fake-refresh-token");
        given(authService.login(any(LoginRequest.class))).willReturn(expectedResponse);

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grantType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("fake-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("fake-refresh-token"))
                .andDo(print());



    }
}
