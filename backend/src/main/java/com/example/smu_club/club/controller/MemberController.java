package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import com.example.smu_club.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final ClubService clubService;

    @GetMapping("/api/v1/member/clubs/{clubId}/apply")
    public ApplicationFormResponseDto getMemberInfoForApplication (
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        return clubService.findMemberInfoWithQuestions(clubId, userDetails);
    }
}
