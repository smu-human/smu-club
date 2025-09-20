package com.example.smu_club.club;

import com.example.smu_club.auth.repository.MemberRepository;
import com.example.smu_club.auth.security.CustomUserDetails;
import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.ClubService;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.Question;
import com.example.smu_club.domain.Role;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.question.repository.QuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.smu_club.domain.QuestionContentType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockito 사용을 위한 확장
class ClubServiceTest {

    @InjectMocks // 테스트 대상 클래스 (Mock 객체들이 여기에 주입됨)
    private ClubService clubService;

    @Mock // 가짜로 만들 의존성 객체
    private ClubRepository clubRepository;

    @Mock // 가짜로 만들 의존성 객체
    private QuestionRepository questionRepository;

    @Mock // <-- 1. MemberRepository에 대한 Mock 추가!
    private MemberRepository memberRepository;


    @Test
    void findMemberInfoWithQuestions_Success() {
        // given (준비)
        Member member = new Member();
        member.setId(1L);
        member.setName("테스트유저");
        member.setStudentId("2020101010");
        member.setPhoneNumber("010-1234-5678");
        member.setRole(Role.MEMBER);

        CustomUserDetails userDetails = new CustomUserDetails(member);

        Club club = new Club();
        club.setId(2L);
        club.setName("테스트동아리");

        List<Question> questions = List.of(
                new Question(club, "지원 동기를 서술하시오.", TEXT, 1),
                new Question(club, "사용 가능한 프로그래밍 언어는?",TEXT, 2)
        );

        // Mock 객체 정의
        // clubRepository.findById()가 어떤 Long 값이든 받으면, 위에서 만든 club 객체를 Optional로 감싸서 반환하도록 설정
        when(clubRepository.findById(anyLong())).thenReturn(Optional.of(club));
        // questionRepository.findAllBy...()가 어떤 Club 객체든 받으면, 위에서 만든 questions 리스트를 반환하도록 설정
        when(questionRepository.findAllByClubOrderByOrderNumAsc(any(Club.class))).thenReturn(questions);
        when(memberRepository.findByStudentId(anyString())).thenReturn(Optional.of(member));

        // when (실행)
        ApplicationFormResponseDto resultDto = clubService.findMemberInfoWithQuestions(2L, userDetails);
        System.out.println("----------");
        System.out.println(resultDto);
        // then (검증)
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getStudentId()).isEqualTo("2020101010");
        assertThat(resultDto.getName()).isEqualTo("테스트유저");
        assertThat(resultDto.getQuestions()).hasSize(2);
        assertThat(resultDto.getQuestions().get(0).getContent()).isEqualTo("지원 동기를 서술하시오.");
    }

    @Test
    void findMemberInfoWithQuestions_Fail_ClubNotFound() {
        // given (준비)
        Member member = new Member();
        member.setId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(member);


        when(clubRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then (실행 및 검증)
        // clubService.findMemberInfoWithQuestions()를 실행했을 때,
        // ClubNotFoundException 예외가 발생하는지 검증
        assertThrows(ClubNotFoundException.class, () -> {
            clubService.findMemberInfoWithQuestions(999L, userDetails);
        });
    }
}