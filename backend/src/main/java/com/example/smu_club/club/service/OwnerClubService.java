package com.example.smu_club.club.service;


import com.example.smu_club.answer.dto.AnswerResponseDto;
import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.repository.ClubImageRepository;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.*;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.util.OciStorageService;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.smu_club.domain.EmailStatus.*;
import static com.example.smu_club.domain.RecruitingStatus.*;

@Slf4j // <-- 이거 하나면 끝! (private static final Logger log = ... 자동 생성됨)
@EnableAsync
@Service
@RequiredArgsConstructor
public class OwnerClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;
    private final OciStorageService ociStorageService;
    private final ClubImageRepository clubImageRepository;
    private final JavaMailSender javaMailSender;

    @Transactional
    public void register(String studentId, ClubInfoRequest request) {

        List<String> uploadedImageFileNames = request.getUploadedImageFileNames();

        String thumbnailUrl = null;
        List<String> clubImagerUrls = new ArrayList<>();

        if (uploadedImageFileNames != null && !uploadedImageFileNames.isEmpty()) {
            for (String uniqueFileName : uploadedImageFileNames) {
                String finalImageUrl = ociStorageService.createFinalOciUrl(uniqueFileName);
                clubImagerUrls.add(finalImageUrl);
            }

            thumbnailUrl = clubImagerUrls.get(0);
        } else { //  기본 썸네일 설정하는거 필요할듯 (사진 받으면 )

        }

        if (clubRepository.existsByName(request.getName())) {
            throw new DuplicateClubNameException("[OWNER] 이미 존재하는 동아리 이름입니다.");
        }

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
                .thumbnailUrl(thumbnailUrl)
                .build();

        clubRepository.save(newClub);

        List<ClubImage> imagesToSave = new ArrayList<>();

        int order = 0;
        for (String imageUrl : clubImagerUrls) {

            ClubImage clubImage = ClubImage.builder()
                    .club(newClub)
                    .imageUrl(imageUrl)
                    .displayOrder(order++)
                    .build();

            imagesToSave.add(clubImage);
        }

        if (!imagesToSave.isEmpty()) {
            clubImageRepository.saveAll(imagesToSave);
        }


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
            throw new AuthorizationException("[OWNER] 동아리 모집을 시작할 권한이 없습니다. ");
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
                .collect(Collectors.toList());

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

    public void validateOwnerAuthority(Long clubId, String studentId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember owner = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 동아리 소속이 아닙니다."));

        if (owner.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("이메일 전송 권한이 없습니다.");
        }
    }


    @Async("mailExecutor") // 호출시 즉시 리턴, 실제 전송은 별도 스레드에서 처리
    @Transactional(readOnly = false)
    public void sendEmailsAsync(Long clubId, List<Long> clubMemberIdList) {
        //1. 클럽 정보 검색
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        //3. 이메일 전송 로직
        for(Long cmId : clubMemberIdList){
            ClubMember applicant = clubMemberRepository.findByIdWithMember(cmId).orElse(null);

            if(applicant == null) {
                log.error("ClubMember ID: {} 찾을 수 없습니다. 이메일 전송을 건너뜁니다.", cmId);
                continue;
            }

            Member m = applicant.getMember(); // 지연 로딩 주의 (Fetch Join 권장)

            String toEmail = m.getEmail();
            String subject = "[SMU-CLUB]  '" + club.getName() + "' 동아리 지원 결과 안내";
            String body = "안녕하세요, " + m.getName() + "님.\n\n" +
                    "귀하의 동아리 '" + club.getName() + "' 지원 결과가 업데이트되었습니다. " +
                    "스뮤클럽에 접속하여 결과를 확인해주시기 바랍니다.\n" +
                    "확인 방법: 홈페이지 접속 -> 로그인 -> 마이페이지 \n\n" +
                    "감사합니다.";

            try{
                //메일 전송 (SMTP)
                sendEmail(toEmail, subject, body);

                //전송 성공시 상태 업데이트
                applicant.setEmailStatus(COMPLETE);
                clubMemberRepository.save(applicant); // Async라 명시적 save 권장

                Thread.sleep(100); // 0.1초 대기
            } catch(SendFailedException e){ //실패시 로그만 남기기
                log.error("이메일 전송 실패: 잘못된 이메일 주소 {}", toEmail, e);
                updateClubMemberEmailStatus(club, m, EmailStatus.FAILED);
            } catch (Exception e) { //실패시 로그만 남기기
                log.error("메일 전송 실패: {}", toEmail, e);
                updateClubMemberEmailStatus(club, m, EmailStatus.FAILED);
            }
        }
    }

    private void updateClubMemberEmailStatus(Club club, Member applicant, EmailStatus emailStatus) {
        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, applicant)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

    }

    private void sendEmail(String toEmail, String subject, String body) throws Exception {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8"); // false indicates not multipart -> no file attachments
            helper.setFrom("no-reply@smuclub.com", "스뮤클럽");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false); // false indicates plain text -> no use HTML
            javaMailSender.send(message);
    }

    @Transactional(readOnly = false)
    public List<Long> fetchPendingAndMarkAsProcessing(Long clubId) {
        //1. clubId 로 Club 찾기
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        //2. clubMember WHERE Club = :club,  status = :findByClubAndEmailStatus.READY 인 사람들 조회
        //여기서 락 걸림.
        List<ClubMember> targets = clubMemberRepository.findByClubAndEmailStatus(club, READY);

        List<Long> toList = new ArrayList<>();

        //3. 조회된 사람들 상태 Processing 으로 변경
        for(ClubMember cm : targets) {
            cm.setEmailStatus(PROCESSING);
            toList.add(cm.getId());

        }

        return toList;
    }
}

