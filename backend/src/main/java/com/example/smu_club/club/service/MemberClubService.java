package com.example.smu_club.club.service;


import com.example.smu_club.answer.dto.AnswerRequestDto;
import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.ClubNotRecruitmentPeriodException;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.MemberNotFoundException;
import com.example.smu_club.exception.custom.QuestionNotFoundException;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.question.dto.QuestionResponse;
import com.example.smu_club.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.smu_club.domain.RecruitingStatus.OPEN;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberClubService {
    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional(readOnly = false)
    public ApplicationResponseDto saveApplication(Long clubId, String studentId, List<AnswerRequestDto> questionAndAnswer, String fileKey) {
        // 1. ClubMember 에 회원 등록 (Status 기본 값은 PENDING)
        Member myInfo = memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberNotFoundException("student id = "+ studentId +"에 해당하는 회원을 찾을 수 없습니다."));

        Club appliedClub = clubRepository.findById(clubId).orElseThrow(() -> new ClubNotFoundException("club id = "+ clubId +"에 해당하는 동아리를 찾을 수 없습니다."));

        ClubMember clubMember = ClubMember.builder()
                .member(myInfo)
                .club(appliedClub)
                .clubRole(ClubRole.MEMBER)
                .appliedAt(LocalDateTime.now())
                .status(ClubMemberStatus.PENDING)
                .emailStatus(EmailStatus.READY)
                .build();

        clubMemberRepository.save(clubMember);

        // 2. 지원서 답변 및 파일 저장 (답변은 질문에 맞게 Mapping 한다.)
        if(questionAndAnswer.isEmpty()) throw new QuestionNotFoundException("clubId = " + clubId + ": 해당 동아리에 등록된 질문을 찾을 수 없습니다.");

        List<Long> questionIds = questionAndAnswer.stream()
                .map(AnswerRequestDto::getQuestionId)
                .collect(toList()
                );

        Map<Long, Question> questionMap = questionRepository.findAllById(questionIds)
                .stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        //답변 타입 "TEXT", "FILE"을 구분하여 저장한다.
        for (AnswerRequestDto ard : questionAndAnswer){
            Question question = questionMap.get(ard.getQuestionId());

            Answer answer = new Answer();
            answer.setQuestion(question);
            // Answer 엔티티에 Member 객체를 넣어줌
            answer.setMember(myInfo);

            if(question.getQuestionContentType() == QuestionContentType.FILE){
                //Blank: 문자가 없거나 모든 문자가 공배인 경우 true 반환
                if(fileKey != null && !fileKey.isBlank()){
                    answer.setFileKey(fileKey);
                }
                else{
                    answer.setFileKey(null);
                }
            }
            else{
                answer.setAnswerContent(ard.getAnswerContent());
            }
            answerRepository.save(answer);
        }

        return new ApplicationResponseDto(questionAndAnswer, fileKey);
    }

    @Transactional
    public ApplicationFormResponseDto findMemberInfoWithQuestions(Long clubId, String studentId){
        /**
         * questions search
         */
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("동아리 ID: "+ clubId +" 해당 동아리를 찾을 수 없습니다."));

        if(club.getRecruitingStatus() != OPEN) throw new ClubNotRecruitmentPeriodException("동아리 ID:"+ clubId +" 해당 동아리는 모집기간이 아닙니다.");

        List<Question> questionList =
                questionRepository.findAllByClubOrderByOrderNumAsc(club);

        //Spring Data JPA의 findAllBy 같은 조회 메서드는 결과가 없을 때 null을 반환하지 않고 empty()인 상태이다.
        //if문에 null아닌 isEmpty()로 설정해야 된다.
        if(questionList.isEmpty()) throw new QuestionNotFoundException("동아리 ID: " + clubId + "의 질문을 찾을 수 없습니다.");

        List<QuestionResponse> clubQuestionListResponse =
                questionList.stream().map
                        (qr -> new QuestionResponse(
                                qr.getId(),
                                qr.getOrderNum(),
                                qr.getContent()
                        )).collect(toList());
        /**
         * Member search
         */
        Member myInfo = memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberNotFoundException("student id = "+ studentId +" is not found"));
        /**
         * Transaction to Dto
         */
        return new ApplicationFormResponseDto(
                myInfo.getId(),
                myInfo.getStudentId(),
                myInfo.getName(),
                myInfo.getPhoneNumber(),
                clubQuestionListResponse
        );
    }
}
