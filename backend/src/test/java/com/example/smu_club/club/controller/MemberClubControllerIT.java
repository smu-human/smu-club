package com.example.smu_club.club.controller;

import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.MemberClubService;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.Question;
import com.example.smu_club.domain.Role;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.smu_club.domain.QuestionContentType.FILE;
import static com.example.smu_club.domain.QuestionContentType.TEXT;
import static com.example.smu_club.domain.RecruitingStatus.CLOSED;
import static com.example.smu_club.domain.RecruitingStatus.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

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

    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        memberRepository.deleteAll();
        questionRepository.deleteAll();
    }


    @Test
    @DisplayName("성공, GET - /api/v1/member/clubs/{clubId}/apply")
    public void 지원하기_내정보_및_질문리스트_가져오기() throws Exception{
        //given
        Member member = Member.builder()
                .studentId("202215064")
                .name("유승준")
                .email("gksrnr66@gmail.com")
                .department("휴먼지능정보공학과")
                .role(Role.MEMBER)
                .phoneNumber("01041301904")
                .build();
        memberRepository.save(member);

        Club club = Club.builder()
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

        Question question1 = Question.builder()
                .club(club)
                .content("나이가 어떻게 되십니까?")
                .questionContentType(TEXT)
                .orderNum(1)
                .build();
        questionRepository.save(question1);

        Question question2 = Question.builder()
                .club(club)
                .content("사진을 업로드 해주세요")
                .questionContentType(FILE)
                .orderNum(2)
                .build();
        questionRepository.save(question2);
        //when
        memberClubService.findMemberInfoWithQuestions(1L, "202215064");

        //then
        //DB테스트는 assertThat을 사용하고, mockMvc는 엔트포인트가 어떻게 응답을 받는지 확인한다.
        //assertThat()
    }



}
