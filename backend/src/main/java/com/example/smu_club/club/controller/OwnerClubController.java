package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ManagedClubResponse;
import com.example.smu_club.club.service.ClubService;
import com.example.smu_club.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
public class OwnerClubController {

    private final ClubService clubService;

    @GetMapping("/managed-clubs")
    public ResponseEntity<ApiResponseDto<List<ManagedClubResponse>>> getMyManagedClubs(
            @AuthenticationPrincipal User user
            ) {
        String memberId = user.getUsername();
        List<ManagedClubResponse> managedClubs = clubService.findManagedClubsByMemberId(memberId);
        ApiResponseDto<List<ManagedClubResponse>> response = ApiResponseDto.success(managedClubs, "운영자의 동아리를 조회합니다.");

        return ResponseEntity.ok(response);
    }
}
