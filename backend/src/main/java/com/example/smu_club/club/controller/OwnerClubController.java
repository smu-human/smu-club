package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ClubInfoRequest;
import com.example.smu_club.club.dto.ManagedClubResponse;
import com.example.smu_club.club.service.ClubService;
import com.example.smu_club.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
public class OwnerClubController {

    private final ClubService clubService;

    // (owner) 동아리 목록 조회  MyPage 기준으로 들어오면 동아리 정보 나옴
    @GetMapping("/managed-clubs")
    public ResponseEntity<ApiResponseDto<List<ManagedClubResponse>>> getMyManagedClubs(
            @AuthenticationPrincipal User user
    ) {
        String memberId = user.getUsername();
        List<ManagedClubResponse> managedClubs = clubService.findManagedClubsByMemberId(memberId);
        ApiResponseDto<List<ManagedClubResponse>> response = ApiResponseDto.success(managedClubs, "운영자의 동아리를 조회합니다.");

        return ResponseEntity.ok(response);
    }

    // 동아리 상세정보 등록
    @PostMapping("/register/club")
    public ResponseEntity<ApiResponseDto<Void>> registerClub(
            @RequestBody ClubInfoRequest request,
            @AuthenticationPrincipal User user
    ) {
        clubService.register(user.getUsername(),request);

        ApiResponseDto<Void> response = ApiResponseDto.success("[Owner] 동아리 정보 등록에 성공했습니다 ");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
