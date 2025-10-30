package com.example.smu_club.club.service;


import com.example.smu_club.answer.dto.AnswerResponseDto;
import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.AuthorizationException;
import com.example.smu_club.exception.custom.ClubMemberNotFoundException;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.MemberNotFoundException;
import com.example.smu_club.member.repository.MemberRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.smu_club.domain.RecruitingStatus.OPEN;

@Service
@RequiredArgsConstructor
public class OwnerClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;


    @Transactional
    public void register(String studentId, ClubInfoRequest request) {

        // 1. 클럽 정보 등록
        Club newClub = Club.builder()
                .name(request.getName())
                .title(request.getTitle())
                .description(request.getDescription())
                .president(request.getPresident())
                .contact(request.getContact())
                .clubRoom(request.getClubRoom())
                .recruitingEnd(request.getRecruitingEnd())
                .recruitingStart(null)
                .recruitingStatus(RecruitingStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        clubRepository.save(newClub);


        // 2. ClubMember 관계 만들어주기

        Member ownerMember = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("학번 :  " + studentId + "를 찾을 수 없습니다."));

        ClubMember clubMember = new ClubMember(
                ownerMember,
                newClub,
                ClubRole.OWNER,
                LocalDateTime.now(),
                ClubMemberStatus.ACCEPTED
        );

        clubMemberRepository.save(clubMember);
    }


    @Transactional(readOnly = true)
    public List<ManagedClubResponse> findManagedClubsByMemberId(String studentId) {

        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 해당 학번의 사용자를 찾을 수 없습니다: " + studentId));

        long memberId = member.getId();

        List<ClubMember> managedClubRelations = clubMemberRepository.findByMemberIdAndClubRoleWithClub(memberId, ClubRole.OWNER);

        return managedClubRelations.stream()
                .map(relation -> new ManagedClubResponse(
                        relation.getClub().getId(),
                        relation.getClub().getName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void startRecruitment(Long clubId, String studentId) {

        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 힉번 : " + studentId + " 를 찾을 수 없습니다. "));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException ("[OWNER] 동아리 모집을 시작할 권한이 없습니다. ");
        }

        club.updateRecruitment(OPEN);


    }

    @Transactional(readOnly = true)
    public ClubInfoResponse getClubInfo(Long clubId, String studentId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] 존재하지 않는 동아리입니다."));


        ClubMember clubMember = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("[OWNER] 동아리를 조회할 권한이 없습니다.. ");
        }

        return ClubInfoResponse.from(club);
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

    @Transactional
    public void updateApplicantStatus(Long clubId, Long clubMemberId, String studentId, @NotNull(message = "변경할 상태를 입력해주세요.") ClubMemberStatus newStatus) {

        // 1. 권한 검증
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("존재하지 않는 동아리 입니다. "));

        ClubMember owner = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 동아리 소속이 아닙니다."));

        if (owner.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("지원자 상태를 변경할 권한이 없습니다.");
        }

        // 2. 대상 지원서 조회
        ClubMember application = clubMemberRepository.findById(clubMemberId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 지원서를 찾을 수 없습니다."));

        if (application.getClub().getId() != clubId) {
            throw new AuthorizationException("해당 동아리 지원서가 아닙니다.");
        }
        // 상태 변경
        application.setStatus(newStatus);
    }
}
