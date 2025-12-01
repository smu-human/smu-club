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
import com.example.smu_club.util.OCICleanupEvent;
import com.example.smu_club.util.OciStorageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.toList;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final OciStorageService ociStorageService;
    private final ClubImageRepository clubImageRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public void register(String studentId, ClubInfoRequest request) {

        Member ownerMember = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 학번 :  " + studentId + "를 찾을 수 없습니다."));

        if (clubRepository.existsByName(request.getName())) {
            throw new DuplicateClubNameException("[OWNER] 이미 존재하는 동아리 이름입니다.");
        }


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

        // 예외 먼저 처리 (검증하기)
        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 힉번 : " + studentId + " 를 찾을 수 없습니다. "));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException ("[OWNER] 동아리 수정/삭제 권한이 없습니다.");
        }

        // Club 이름 중복 확인 (이름이 변경된 경우에만)
        if (!club.getName().equals(request.getName()) && clubRepository.existsByName(request.getName())) {
            throw new DuplicateClubNameException("[OWNER] 이미 존재하는 동아리 이름입니다.");
        }

        // 새로운 이미지 URL 목록 생성
        List<String> newUniqueFileNames = request.getUploadedImageFileNames();
        List<String> newImagesUrls = newUniqueFileNames.stream()
                .map(ociStorageService::createFinalOciUrl)
                .toList();

        // 썸네일 결정
        String newThumbnailUrl = newImagesUrls.isEmpty() ? null : newImagesUrls.getFirst();

        // 기존 DB 이미지 URL 조회 및 리스트로만들어두기
        List<ClubImage> existingImages = clubImageRepository.findAllByClubId(clubId);
        List<String> existingUrls = existingImages.stream()
                .map(ClubImage::getImageUrl)
                .toList();

        // 삭제할 URL  (OciStorage 에서 삭제해야돼서)
        List<String> urlsToDeleteFromOci = existingUrls.stream()
                .filter(url -> !newImagesUrls.contains(url))
                .toList();

        club.updateInfo(request, newThumbnailUrl);

        // 다 삭제 시키고 이제 다시 만들어줌
        clubImageRepository.deleteAll(existingImages);

        List<ClubImage> imageToSave = new ArrayList<>();
        int order = 0;
        for (String newImagesUrl : newImagesUrls) {
            ClubImage newImage = ClubImage.builder()
                    .club(club)
                    .imageUrl(newImagesUrl)
                    .displayOrder(order)
                    .build();
            imageToSave.add(newImage);
        }
        // 여기서 이제 DB 트랜잭션 종료
        if (!imageToSave.isEmpty()) {
            clubImageRepository.saveAll(imageToSave);
        }

        if (!urlsToDeleteFromOci.isEmpty()) {
            eventPublisher.publishEvent(new OCICleanupEvent(urlsToDeleteFromOci));
        }

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
}
