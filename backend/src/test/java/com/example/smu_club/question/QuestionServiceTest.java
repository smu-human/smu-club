package com.example.smu_club.question;


import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Question;
import com.example.smu_club.domain.QuestionContentType;
import com.example.smu_club.question.dto.QuestionRequest;
import com.example.smu_club.question.dto.QuestionResponse;
import com.example.smu_club.question.repository.ClubRepository2;
import com.example.smu_club.question.repository.QuestionRepository;
import com.example.smu_club.question.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
 class QuestionServiceTest {

    @InjectMocks
    private QuestionService questionService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ClubRepository2 clubRepository2;

    @Test
    @DisplayName("동아리 ID로 질문 목록 조회 성공")
    void findQuestionsByClubId_success() {
        // given (준비)
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        List<Question> questions = List.of(
                Question.builder().club(club).content("질문1").orderNum(1).build(),
                Question.builder().club(club).content("질문2").orderNum(2).build()
        );

        // clubRepository2.findById(clubId)가 호출되면, Optional.of(club)을 반환하라고 정의
        given(clubRepository2.findById(clubId)).willReturn(Optional.of(club));
        // questionRepository.findAllByClubOrderByOrderNumAsc(club)가 호출되면, questions 리스트를 반환하라고 정의
        given(questionRepository.findAllByClubOrderByOrderNumAsc(club)).willReturn(questions);

        // when (실행)
        List<QuestionResponse> result = questionService.findQuestionsByClubId(clubId);

        // then (검증)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("질문1");
        assertThat(result.get(1).getOrderNum()).isEqualTo(2);

        // Mock 객체들의 메서드가 정확히 1번씩 호출되었는지 검증
        verify(clubRepository2).findById(clubId);
        verify(questionRepository).findAllByClubOrderByOrderNumAsc(club);
    }

    @Test
    @DisplayName("질문 목록 저장 성공")
    void saveQuestions_success() {
        // given (준비)
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);

        List<QuestionRequest> requestList = List.of(
                new QuestionRequest(1,"새로운 질문1 입니다."),
                new QuestionRequest(2,"새로운 질문2 입니다.")
        );

        // clubRepository가 clubId로 club을 성공적으로 찾아온다고 가정
        given(clubRepository2.findById(clubId)).willReturn(Optional.of(club));

        // when (실행)
        questionService.saveQuestions(clubId, requestList);

        // then (검증)
        // 1. findById가 1번 호출되었는지 검증
        verify(clubRepository2).findById(clubId);

        // 2. deleteAll.. 메서드가 TEXT와 FILE 타입에 대해 각각 1번씩 호출되었는지 검증
        verify(questionRepository).deleteAllByClubAndQuestionContentType(club, QuestionContentType.TEXT);
        verify(questionRepository).deleteAllByClubAndQuestionContentType(club, QuestionContentType.FILE);

        // 3. saveAll에 전달된 List<Question> 객체를 캡처하여 내용 검증
        ArgumentCaptor<List<Question>> textQuestionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(textQuestionsCaptor.capture()); // 캡처!
        List<Question> capturedTextQuestions = textQuestionsCaptor.getValue();

        assertThat(capturedTextQuestions).hasSize(2);
        assertThat(capturedTextQuestions.get(0).getContent()).isEqualTo("새로운 질문1 입니다.");
        assertThat(capturedTextQuestions.get(0).getOrderNum()).isEqualTo(1);
        assertThat(capturedTextQuestions.get(1).getContent()).isEqualTo("새로운 질문2 입니다.");
        assertThat(capturedTextQuestions.get(1).getOrderNum()).isEqualTo(2);

        // 4. 마지막 save에 전달된 fileQuestion 객체를 캡처하여 내용 검증
        ArgumentCaptor<Question> fileQuestionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(fileQuestionCaptor.capture()); // 캡처!
        Question capturedFileQuestion = fileQuestionCaptor.getValue();

        assertThat(capturedFileQuestion.getQuestionContentType()).isEqualTo(QuestionContentType.FILE);
        assertThat(capturedFileQuestion.getContent()).isEqualTo("파일을 업로드 해주세요(선택)");
        assertThat(capturedFileQuestion.getOrderNum()).isEqualTo(3); // text 질문이 2개였으므로 다음 순서는 3
    }
}
