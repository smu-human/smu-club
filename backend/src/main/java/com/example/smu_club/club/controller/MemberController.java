package com.example.smu_club.club.controller;


import com.example.smu_club.auth.security.CustomUserDetails;
import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/api/v1//api/v1/member/clubs/{clubId}/apply")
    public ApplicationFormResponseDto getMemberInfoForApplication (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int clubId) {

    }
}
