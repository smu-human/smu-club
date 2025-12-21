package com.example.smu_club.club.service;


import com.example.smu_club.answer.dto.AnswerResponseDto;
import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.repository.ClubImageRepository;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.common.fileMetaData.FileMetaDataService;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.*;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.util.ExcelService;
import com.example.smu_club.util.oci.OciStorageService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.smu_club.domain.EmailStatus.*;
import static com.example.smu_club.domain.RecruitingStatus.*;

@Slf4j
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
    private final ExcelService excelService;
    private final FileMetaDataService fileMetadataService;

    private final JavaMailSender javaMailSender;

    @Transactional
    public void register(String studentId, ClubInfoRequest request) {
        // 1. 유효성 검증 및 기본 정보 조회
        Member ownerMember = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 학번 : " + studentId + "를 찾을 수 없습니다."));

        if (clubRepository.existsByName(request.getName())) {
            throw new DuplicateClubNameException("[OWNER] 이미 존재하는 동아리 이름입니다.");
        }

        // 2. 이미지 데이터 준비
        List<String> uploadedImageFileNames = request.getUploadedImageFileNames();
        String thumbnailKey = "default_thumbnail.png"; // 기본값 설정
        List<ClubImage> imagesToSave = new ArrayList<>();

        if (uploadedImageFileNames != null && !uploadedImageFileNames.isEmpty()) {

            // 상태를 ACTIVE 로 저장 (나중에 배치처리를 위해)
            fileMetadataService.updateStatus(uploadedImageFileNames, FileStatus.ACTIVE);
            // 첫 번째 사진을 썸네일로 지정
            thumbnailKey = uploadedImageFileNames.get(0);

            // 상세 이미지 객체 생성
            for (int i = 0; i < uploadedImageFileNames.size(); i++) {
                imagesToSave.add(ClubImage.builder()
                        .imageFileKey(uploadedImageFileNames.get(i))
                        .displayOrder(i)
                        .build());
            }
        }

        // 3. Club 엔티티 생성 및 저장
        Club newClub = Club.builder()
                .name(request.getName())
                .title(request.getTitle())
                .description(request.getDescription())
                .president(request.getPresident())
                .contact(request.getContact())
                .clubRoom(request.getClubRoom())
                .recruitingEnd(request.getRecruitingEnd())
                .recruitingStatus(RecruitingStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .thumbnailUrl(thumbnailKey)
                .build();

        clubRepository.save(newClub); // 연관 관계 설정을 위해 먼저 저장

        // 4. 상세 이미지 연관 관계 설정 및 저장
        if (!imagesToSave.isEmpty()) {
            imagesToSave.forEach(image -> image.setClub(newClub)); // Club 연결
            clubImageRepository.saveAll(imagesToSave);
        }

        // 5. ClubMember(Owner) 권한 등록
        ClubMember clubMember = new ClubMember(
                ownerMember,
                newClub,
                ClubRole.OWNER,
                LocalDateTime.now(),
                ClubMemberStatus.ACCEPTED
        );
        clubMemberRepository.save(clubMember);
    }

    @Transactional
    public void updateClub(Long clubId, String studentId, ClubInfoRequest request) {

        // 1. 권한 검증 및 Club
        Club club = getValidatedClubAsOwner(clubId, studentId);

        // Club 이름 중복 확인 (이름이 변경된 경우에만)
        if (!club.getName().equals(request.getName()) && clubRepository.existsByName(request.getName())) {
            throw new DuplicateClubNameException("[OWNER] 이미 존재하는 동아리 이름입니다.");
        }

        // 새로운 이미지 URL 목록 생성
        List<String> newFileKeys = request.getUploadedImageFileNames();
        String newThumbnailKey = newFileKeys.isEmpty() ? null : newFileKeys.getFirst();

        List<ClubImage> existingImages = clubImageRepository.findAllByClubId(clubId);
        List<String> existingFileKeys = existingImages.stream()
                .map(ClubImage::getImageFileKey)
                .toList();

        List<String> keysToDelete = existingFileKeys.stream()
                .filter(key -> !newFileKeys.contains(key))
                .toList();

        // 새로 추가된 파일들에 대해서 PENDING -> ACTIVE
        if (!newFileKeys.isEmpty()) {
            fileMetadataService.updateStatus(newFileKeys, FileStatus.ACTIVE);
        }
        // 버려진 기존 파일들에 대해서 ACTIVE -> DELETED
        if (!keysToDelete.isEmpty()) {
            fileMetadataService.updateStatus(newFileKeys, FileStatus.DELETED);
        }

        club.updateInfo(request, newThumbnailKey);

        clubImageRepository.deleteAll(existingImages);

        // 새로운 연결 정보 생성
        List<ClubImage> imagesToSave = new ArrayList<>();
        for (int i = 0; i < newFileKeys.size(); i++) {
            imagesToSave.add(ClubImage.builder()
                    .club(club)
                    .imageFileKey(newFileKeys.get(i))
                    .displayOrder(i)
                    .build());
        }

        clubImageRepository.saveAll(imagesToSave);
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

        Club club = getValidatedClubAsOwner(clubId, studentId);

        club.updateRecruitment(OPEN);
    }

    @Transactional(readOnly = true)
    public ClubInfoResponse getClubInfo(Long clubId, String studentId) {

        Club club = getValidatedClubWithClubImagesAsOwner(clubId, studentId);

        return ClubInfoResponse.from(club, ociStorageService::createFinalOciUrl);
    }

    @Transactional(readOnly = true)
    public List<ApplicantResponse> getApplicantList(Long clubId, String studentId) {

        Club club = getValidatedClubAsOwner(clubId, studentId);

        List<ClubMember> applicants = clubMemberRepository.findByClubAndStatus(club, ClubMemberStatus.PENDING);

        return applicants.stream()
                .map(ApplicantResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicantDetailViewResponse getApplicantDetails(Long clubMemberId, String studentId, Long clubId) {

        Club club = getValidatedClubAsOwner(clubId, studentId);

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
                            ? answer.getFileKey()
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

        getValidatedClubAsOwner(clubId, studentId);

        // 2. 대상 지원서 조회
        ClubMember application = clubMemberRepository.findById(clubMemberId)
                .orElseThrow(() -> new ClubMemberNotFoundException("해당 지원서를 찾을 수 없습니다."));

        if (application.getClub().getId() != clubId) {
            throw new AuthorizationException("해당 동아리 지원서가 아닙니다.");
        }
        // 상태 변경
        application.setStatus(newStatus);
    }

    public Club getValidatedClubAsOwner(Long clubId, String studentId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember owner = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (owner.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("[OWNER] 해당 권한이 없습니다.");
        }

        return club;
    }
    public Club getValidatedClubWithClubImagesAsOwner(Long clubId, String studentId) {

        Club club = clubRepository.findByIdWithClubImages(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember owner = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (owner.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("[OWNER] 해당 권한이 없습니다.");
        }

        return club;
    }


    @Async("mailExecutor") // 호출시 즉시 리턴, 실제 전송은 별도 스레드에서 처리
    public void sendEmailsAsync(Long clubId, List<Long> clubMemberIdList) {
        //1. 클럽 정보 검색
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        //2. 상태를 담는 리스트 생성
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();


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
                successIds.add(applicant.getId());

                Thread.sleep(100); // 0.1초 대기
            } catch(SendFailedException e){ //실패시 로그만 남기기
                log.error("이메일 전송 실패: 잘못된 이메일 주소 {}", toEmail, e);
                failedIds.add(applicant.getId());
            } catch (Exception e) { //실패시 로그만 남기기
                log.error("메일 전송 실패: {}", toEmail, e);
                failedIds.add(applicant.getId());
            }
        }
        updateEmailStatuses(successIds, COMPLETE);
        updateEmailStatuses(failedIds, FAILED);

    }

    private void updateEmailStatuses(List<Long> clubMemberIds, EmailStatus emailStatus) {
        clubMemberRepository.BulkUpdateEmailStatusByIds(clubMemberIds, emailStatus); //@Transactional 적용

        if(emailStatus != FAILED) {
            log.info("이메일 전송 실패 보장 트랜잭션 처리 : {} 건", clubMemberIds.size());

        }
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

    public byte[] downloadAcceptedMembersExcel(Long clubId, String studentId) {

        getValidatedClubAsOwner(clubId, studentId);

        // ROLE 이 MEMBER 이고 해당 클럽 지원한 지원자들 전부 불러옴
        List<ClubMember> allMembers = clubMemberRepository.findAllByClubIdAndRoleWithMember(clubId, ClubRole.MEMBER);

        List<ApplicantExcelDto> excelDtos = allMembers.stream()
                .sorted(
                        Comparator.comparing((ClubMember c) ->
                                c.getStatus() == ClubMemberStatus.ACCEPTED ? 0 : 1)

                                .thenComparing(c -> c.getMember().getName())
                )
                .map(ApplicantExcelDto::from)
                .toList();

        return excelService.createApplicantExcel(excelDtos);
    }
}

