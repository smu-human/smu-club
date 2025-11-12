package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.*;
import com.example.smu_club.club.service.MemberClubService;
import com.example.smu_club.club.service.OwnerClubService;
import com.example.smu_club.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner/club")
public class OwnerClubController {

    private final OwnerClubService ownerClubService;
    private final MemberClubService memberClubService;

    // (owner) 동아리 목록 조회  MyPage 기준으로 들어오면 동아리 정보 나옴
    @GetMapping("/managed-clubs")
    public ResponseEntity<ApiResponseDto<List<ManagedClubResponse>>> getMyManagedClubs(
            @AuthenticationPrincipal User user
    ) {
        String memberId = user.getUsername();
        List<ManagedClubResponse> managedClubs = ownerClubService.findManagedClubsByMemberId(memberId);
        ApiResponseDto<List<ManagedClubResponse>> response = ApiResponseDto.success(managedClubs, "[OWNER] 운영자의 동아리를 조회합니다.");

        return ResponseEntity.ok(response);
    }

    // 동아리 상세정보 조회 (편집하기 위해서는 정보를 받아와야됨)
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponseDto<ClubInfoResponse>> getClubInfo(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {

        ClubInfoResponse clubInfoResponse = ownerClubService.getClubInfo(clubId, user.getUsername());

        ApiResponseDto<ClubInfoResponse> response = ApiResponseDto.success(clubInfoResponse, "[OWNER] 클럽 조회에 성공했습니다.");
        return ResponseEntity.ok(response);

    }

    // 동아리 상세정보 등록
    @PostMapping(value = "/register/club", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> registerClub(
            @ModelAttribute ClubInfoRequest request,
            @AuthenticationPrincipal User user
    ) {
        ownerClubService.register(user.getUsername(), request);

        ApiResponseDto<Void> response = ApiResponseDto.success("[Owner] 동아리 정보 등록에 성공했습니다. ");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 동아리 상태 변경 (UPCOMING -> OPEN)
    @PostMapping("/{clubId}/start-recruitment")
    public ResponseEntity<ApiResponseDto<Void>> startRecruitment(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {

        ownerClubService.startRecruitment(clubId, user.getUsername());

        ApiResponseDto<Void> response = ApiResponseDto.success("[OWNER] 동아리 모집을 성공적으로 시작했습니다.");
        return ResponseEntity.ok(response);
    }

    // 동아리 지원자 리스트 조회
    @GetMapping("/{clubId}/applicants")
    public ResponseEntity<ApiResponseDto<List<ApplicantResponse>>> getClubApplicants(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {
        List<ApplicantResponse> applicants = ownerClubService.getApplicantList(clubId, user.getUsername());

        ApiResponseDto<List<ApplicantResponse>> response = ApiResponseDto.success(applicants, "[OWNER] 지원자 조회에 성공했습니다.");
        return ResponseEntity.ok(response);
    }

    //  (동아리 관리자)가 1단계 목록에서 특정 지원자(예: "홍길동")를 클릭했을 때 - 지원자 상세정보 + 질문 및 답변
    @GetMapping("/{clubId}/applicants/{clubMemberId}")
    public ResponseEntity<ApiResponseDto<ApplicantDetailViewResponse>> getApplicantDetails(
            @PathVariable Long clubId,
            @PathVariable Long clubMemberId,
            @AuthenticationPrincipal User user
    ) {

        String studentId = user.getUsername();

        ApplicantDetailViewResponse applicantData = ownerClubService.getApplicantDetails(
                clubMemberId,
                studentId,
                clubId
        );

        ApiResponseDto<ApplicantDetailViewResponse> response = ApiResponseDto.success(applicantData, "[OWNER] 지원자 상세 정보 조회 성공");
        return ResponseEntity.ok(response);
    }

    // 멤버 거절할지 받을지 결정하는 API
    @PatchMapping("/{clubId}/applicants/{clubMemberId}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateApplicantStatus(
            @PathVariable Long clubId,
            @PathVariable Long clubMemberId,
            @Valid @RequestBody ApplicantStatusUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        ownerClubService.updateApplicantStatus(
                clubId,
                clubMemberId,
                user.getUsername(),
                request.getNewStatus() //  ACCEPTED, REJECTED중 하나
        );

        ApiResponseDto<Void> response = ApiResponseDto.success("[OWNER] 지원자 상태가 성공적으로 변경되었습니다. ");
        return ResponseEntity.ok(response);

    }
}
