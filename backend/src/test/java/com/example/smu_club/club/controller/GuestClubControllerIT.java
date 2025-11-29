/*
package com.example.smu_club.club.controller;


import com.example.smu_club.auth.dto.JwtTokenResponse;
import com.example.smu_club.auth.jwt.JwtTokenProvider;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.GuestClubService;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.RecruitingStatus;
import com.example.smu_club.domain.Role;
import com.example.smu_club.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "JWT_SECRET=dummydummydummydummydummydummydummydummydummydummydummydummy"
})
public class GuestClubControllerIT {
    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider; // 토큰 생성기가 있다고 가정
    @Autowired private GuestClubService guestClubService;

    private Member testMember;
    private Club club1, club2, club3;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        clubRepository.deleteAll();

        */
/**
         * 유저 생성
         *//*


        this.testMember = Member.builder()
                .studentId("202215064")
                .name("유승준")
                .email("gksrnr66@gmail.com")
                .department("휴먼지능정보공학과")
                .role(Role.MEMBER)
                .phoneNumber("01041301904")
                .build();
        memberRepository.save(testMember);

        JwtTokenResponse token = jwtTokenProvider.generateToken(testMember);
        this.testMember.updateRefreshToken(token.getRefreshToken());
        memberRepository.save(this.testMember);


        */
/**
         * 동아리 생성
         *//*

        club1 = Club.builder()
                .name("동아리1")
                .title("test1")
                .createdAt(LocalDateTime.now())
                .recruitingStatus(RecruitingStatus.CLOSED)
                .recruitingStart(LocalDate.now())
                .recruitingEnd(LocalDate.now().plusDays(7))
                .president("유승준1")
                .contact("01041301904")
                .clubRoom("G308")
                .thumbnailUrl("test1")
                .build();

        club2 = Club.builder()
                .name("동아리2")
                .title("test2")
                .createdAt(LocalDateTime.now())
                .recruitingStatus(RecruitingStatus.UPCOMING)
                .recruitingStart(LocalDate.now())
                .recruitingEnd(LocalDate.now().plusDays(6))
                .president("유승준2")
                .contact("01041301904")
                .clubRoom("G308")
                .thumbnailUrl("test2")
                .build();

        club3 = Club.builder()
                .name("동아리3")
                .title("test3")
                .createdAt(LocalDateTime.now())
                .recruitingStatus(RecruitingStatus.OPEN)
                .recruitingStart(LocalDate.now())
                .recruitingEnd(LocalDate.now().plusDays(5))
                .president("유승준3")
                .contact("01041301904")
                .clubRoom("G308")
                .thumbnailUrl("test3")
                .build();
        clubRepository.saveAll(List.of(club1, club2, club3));
    }

    @Test
    @DisplayName("GET - /api/v1/public/clubs")
    public void 메인페이지_동아리목록_가져오기() throws Exception {

        //When & Then
        mockMvc.perform(get("/api/v1/public/clubs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //HTTP 200 OK 확인
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray()) //Json 응답이 배열 형태인지 확인
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("동아리1"))
                .andExpect(jsonPath("$.data[0].recruitingStatus").value("CLOSED"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("동아리2"))
                .andExpect(jsonPath("$.data[1].recruitingStatus").value("UPCOMING"))
                .andExpect(jsonPath("$.data[2].id").value(3))
                .andExpect(jsonPath("$.data[2].name").value("동아리3"))
                .andExpect(jsonPath("$.data[2].recruitingStatus").value("OPEN"))
                .andDo(print()); // 요청/응답 내용을 콘솔에 출력
    }

    @Test
    @DisplayName("GET - /api/v1/public/clubs/{clubId}  성공")
    public void 메인페이지_동아리상세페이지_가져오기() throws Exception{
        //Given
        Long existingClubId = club1.getId();

        //When & Then
        mockMvc.perform(get("/api/v1/public/clubs/" + existingClubId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(existingClubId))
                .andDo(print());
    }

    @Test
    @DisplayName("GET - /api/v1/public/clubs/{clubId}  실패")
    public void 메인페이지_동아리상세페이지_가져오기_실패() throws Exception{
        //Given
        Long notExistingClubId = 999L;

        //When & Then
        mockMvc.perform(get("/api/v1/public/clubs/" + notExistingClubId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.errorCode").value("CLUB_NOT_FOUND"))
                .andDo(print());

    }
}
*/
