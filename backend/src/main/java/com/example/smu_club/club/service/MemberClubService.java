package com.example.smu_club.club.service;


import com.example.smu_club.answer.dto.AnswerRequestDto;
import com.example.smu_club.answer.dto.AnswerResponseDto;
import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.*;
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
public class MemberClubService {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public ApplicationResponseDto saveApplication(Long clubId, String studentId, List<AnswerRequestDto> QuestionAndAnswer, String fileUrl) {
        // 1. ClubMember 에 회원 등록 (Status 기본 값은 PENDING)
        Member myInfo = memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberNotFoundException("student id = "+ studentId +" is not found"));
        Club appliedClub = clubRepository.findById(clubId).orElseThrow(() -> new ClubNotFoundException("club id = "+ clubId +" is not found"));
        ClubMember clubMember = new ClubMember(myInfo, appliedClub, ClubRole.MEMBER, LocalDateTime.now(), ClubMemberStatus.PENDING);
        clubMemberRepository.save(clubMember);

        // 2. 지원서 답변 및 파일 저장 (답변은 질문에 맞게 Mapping 한다.)
        if(QuestionAndAnswer.isEmpty()) throw new QuestionNotFoundException("clubId = " + clubId + ": 해당 동아리에 등록된 질문을 찾을 수 없습니다.");

        List<Long> questionIds = QuestionAndAnswer.stream()
                .map(AnswerRequestDto::getQuestionId)
                .collect(toList()
                );

        Map<Long, Question> questionMap = questionRepository.findAllById(questionIds)
                .stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        //답변 타입 "TEXT", "FILE"을 구분하여 저장한다.
        for (AnswerRequestDto ard : QuestionAndAnswer){
            Question question = questionMap.get(ard.getQuestionId());

            Answer answer = new Answer();
            answer.setQuestion(question);
            // Answer 엔티티에 Member 객체를 넣어줌
            answer.setMember(myInfo);

            if(question.getQuestionContentType() == QuestionContentType.FILE){
                answer.setFileUrl(fileUrl);
            }
            else{
                answer.setAnswerContent(ard.getAnswerContent());
            }
            answerRepository.save(answer);
        }

        return new ApplicationResponseDto(QuestionAndAnswer, fileUrl);
    }

    @Transactional
    public ApplicationFormResponseDto findMemberInfoWithQuestions(Long clubId, String studentId){
        /**
         * questions search
         */
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("동아리 ID: "+ clubId +" 해당 동아리를 찾을 수 없습니다."));

        if(club.getRecruitingStatus() != OPEN) throw new ClubNotRecruitmentPeriod("동아리 ID:"+ clubId +" 해당 동아리는 모집기간이 아닙니다.");

        List<Question> questionList =
                questionRepository.findAllByClubOrderByOrderNumAsc(club);
        if(questionList == null) throw new QuestionNotFoundException("동아리 ID: " + clubId + "의 질문을 찾을 수 없습니다.");

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


    @Transactional(readOnly = true)
    public List<ApplicantResponse> getApplicantList(Long clubId, String studentId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 존재하지 않는 동아리입니다."));

        ClubMember clubMember = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아닙니다. "));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("[OWNER] 지원자 목록을 조회할 권한이 없습니다.");
        }

        List<ClubMember> applicants = clubMemberRepository.findByClubAndStatus(club, ClubMemberStatus.PENDING);

        return applicants.stream()
                .map(ApplicantResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicantDetailViewResponse getApplicantDetails(Long clubMemberId, String studentId, Long clubId) {

        // 1. 권한 검증
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("존재하지 않는 동아리 입니다. "));

        ClubMember owner = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 동아리 소속이 아닙니다."));

        if (owner.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("지원자 상세 정보를 조회할 권한이 없습니다.");
        }

        // 2. 데이터 조회
        ClubMember application = clubMemberRepository.findById(clubMemberId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 지원서를 찾을 수 없습니다. ID: " + clubMemberId));

        if (application.getClub().getId() != clubId) {
            throw new AuthorizationException("해당 동아리 지원서가 아닙니다.");
        }

        Member applicationMember = application.getMember();

        ApplicantInfoResponse applicantInfo = ApplicantInfoResponse.builder()
                .clubMemberId(application.getId())
                .memberId(applicationMember.getId())
                .name(applicationMember.getName())
                .studentId(applicationMember.getStudentId())
                .department(applicationMember.getDepartment())
                .phoneNumber(applicationMember.getPhoneNumber())
                .email(applicationMember.getEmail())
                .appliedAt(application.getAppliedAt())
                .build();

        // 3. 질문+답변 만들기
        List<Answer> answers = answerRepository.findByMemberAndClubWithQuestions(applicationMember, club);

        List<AnswerResponseDto> applicationForm = answers.stream()
                .map(answer -> {
                    Question question = answer.getQuestion();
                    String content = (question.getQuestionContentType() == QuestionContentType.FILE)
                            ? answer.getFileUrl()
                            : answer.getAnswerContent();

                    return new AnswerResponseDto(
                            question.getId(),
                            question.getOrderNum(),
                            question.getContent(),
                            content
                    );
                })
                .collect(toList());

        // 최종 DTO 반환
        return ApplicantDetailViewResponse.builder()
                .applicantInfo(applicantInfo)
                .applicationForm(applicationForm)
                .build();
    }
}
