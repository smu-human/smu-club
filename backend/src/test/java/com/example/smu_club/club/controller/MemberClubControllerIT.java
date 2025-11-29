/*
package com.example.smu_club.club.controller;

import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.MemberClubService;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.Question;
import com.example.smu_club.domain.Role;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.ClubNotRecruitmentPeriodException;
import com.example.smu_club.exception.custom.QuestionNotFoundException;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.question.repository.QuestionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.smu_club.domain.QuestionContentType.FILE;
import static com.example.smu_club.domain.QuestionContentType.TEXT;
import static com.example.smu_club.domain.RecruitingStatus.CLOSED;
import static com.example.smu_club.domain.RecruitingStatus.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
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
public class MemberClubControllerIT {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberClubService memberClubService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private MockMvc mockMvc;

    private Member member;
    private Club club;
    private Question question1;
    private Question question2;
    private long clubId;
    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        memberRepository.deleteAll();
        questionRepository.deleteAll();

        member = Member.builder()
                .studentId("202215064")
                .name("유승준")
                .email("gksrnr66@gmail.com")
                .department("휴먼지능정보공학과")
                .role(Role.MEMBER)
                .phoneNumber("01041301904")
                .build();
        memberRepository.save(member);

         club = Club.builder()
                .name("슴우클럽")
                .title("상명대 동아리 사이트 개발 중!!")
                .description("저희 클럽은 개쩌는 개발자 3명으로 구성되어 있는 드림팀입니다.")
                .createdAt(LocalDateTime.now())
                .recruitingStatus(OPEN)
                .recruitingStart(LocalDate.of(2025,9,25))
                .recruitingEnd(LocalDate.of(2025,10,20))
                .president("유승준")
                .contact("01041301904")
                .clubRoom("G308")
                .build();
        clubRepository.save(club);

        clubId = club.getId();

         question1 = Question.builder()
                .club(club)
                .content("나이가 어떻게 되십니까?")
                .questionContentType(TEXT)
                .orderNum(1)
                .build();
        questionRepository.save(question1);

         question2 = Question.builder()
                .club(club)
                .content("사진을 업로드 해주세요")
                .questionContentType(FILE)
                .orderNum(2)
                .build();
        questionRepository.save(question2);
    }


    @Test
    @WithMockUser(username = "202215064", roles = "MEMBER")
    @DisplayName("성공, GET - /api/v1/member/clubs/{clubId}/apply")
    public void 지원하기_내정보_및_질문리스트_가져오기() throws Exception{
        //when
        ApplicationFormResponseDto result = memberClubService.findMemberInfoWithQuestions(1L, "202215064");

        //then
        //DB테스트는 assertThat을 사용하고, mockMvc는 엔트포인트가 어떻게 응답을 받는지 확인한다.
        assertThat(result.getMemberId()).isEqualTo(member.getId()); //member
        assertThat(result.getQuestions()).hasSize(2); //quesiton size
        assertThat(result.getQuestions().getFirst().getQuestionId()).isEqualTo(question1.getId()); //List<QuestionResponse>
        assertThat(result.getQuestions().getFirst().getContent()).isEqualTo(question1.getContent()); //List<QuestionResponse>
        long clubId = club.getId();

        mockMvc.perform(get("/api/v1/member/clubs/"+clubId+"/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) //HTTP 200 OK 확인
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.studentId").value("202215064"))
                .andExpect(jsonPath("$.data.name").value("유승준"))
                .andExpect(jsonPath("$.data.phone").value("01041301904"))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "202215064", roles = "MEMBER")
    @DisplayName("실패: 존재하지 않는 동아리")
    public void 지원하기_실패_동아리_없음() throws Exception {
        //when & then
        long nonExistentClubId = 999L; // 존재하지 않는 동아리 ID

        RuntimeException e1 = Assertions.assertThrows(
                ClubNotFoundException.class,
                () -> memberClubService.findMemberInfoWithQuestions(nonExistentClubId, "202215064"));

        mockMvc.perform(get("/api/v1/member/clubs/" + nonExistentClubId + "/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) //HTTP 404
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.errorCode").value("CLUB_NOT_FOUND"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "202215064", roles = "MEMBER")
    @DisplayName("실패: 동아리 모집상태 불일치")
    public void 지원하기_실패_모집상태_불일치() throws Exception{
        //when & then
        club.setRecruitingStatus(CLOSED);
        clubRepository.save(club);
        RuntimeException e2 = Assertions.assertThrows(
                ClubNotRecruitmentPeriodException.class,
                () -> memberClubService.findMemberInfoWithQuestions(clubId, "202215064"));

        //엔드 포인트
        mockMvc.perform(get("/api/v1/member/clubs/" + clubId + "/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) //HTTP 404
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.errorCode").value("CLUB_NOT_RECRUITMENT_PERIOD"))
                .andDo(print());

    }

    @Test
    @WithMockUser(username = "202215064", roles = "MEMBER")
    @DisplayName("실패: 동아리 질문 없음")
    public void 지원하기_실패_질문_없음() throws Exception{
        //when & then
        long clubId = club.getId();
        club.setRecruitingStatus(OPEN);
        clubRepository.save(club);
        deleteQuestionInClub(question1);
        deleteQuestionInClub(question2);

        RuntimeException e3 = Assertions.assertThrows(
                QuestionNotFoundException.class,
                () -> memberClubService.findMemberInfoWithQuestions(clubId, "202215064"));

        //엔드 포인트
        mockMvc.perform(get("/api/v1/member/clubs/" + clubId + "/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) //HTTP 404
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.errorCode").value("QUESTION_NOT_FOUND"))
                .andDo(print());

    }

    private void deleteQuestionInClub(Question question) {
        question.setClub(null);
        questionRepository.save(question);
    }
}
*/
