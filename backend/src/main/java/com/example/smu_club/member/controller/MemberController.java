package com.example.smu_club.member.controller;

import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.member.dto.ApplicationListResponseDto;
import com.example.smu_club.member.dto.ApplicationResultResponseDto;
import com.example.smu_club.member.dto.MemberNameResponseDto;
import com.example.smu_club.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.smu_club.common.ApiResponseDto.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/mypage/name")
    public ResponseEntity<ApiResponseDto<MemberNameResponseDto>> findMemberName(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String studentId = userDetails.getUsername();
        MemberNameResponseDto name = memberService.MyName(studentId);
        return ResponseEntity.ok(success(name, "이름 조회 성공 [마이페이지]"));
    }

    @GetMapping("/mypage/applications")
    public ResponseEntity<ApiResponseDto<List<ApplicationListResponseDto>>> findApplications(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();
        List<ApplicationListResponseDto> appList = memberService.findApplications(studentId);
        return ResponseEntity.ok(success(appList, "지원 목록 조회 성공"));
    }

    @GetMapping("/mypage/applications/{clubId}/result")
    public ResponseEntity<ApiResponseDto<ApplicationResultResponseDto>> findConfirmation(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();
        ApplicationResultResponseDto result = memberService.findResult(studentId, clubId);
        return ResponseEntity.ok(success(result, "지원 결과 조회 성공"));
    }
}
