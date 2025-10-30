package com.example.smu_club.member.controller;

import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.member.dto.*;
import com.example.smu_club.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
        List<ApplicationListResponseDto> applications = memberService.findApplications(studentId);

        return ResponseEntity.ok(success(applications));
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


    @GetMapping("/mypage/applications/{clubId}/edit")
    public ResponseEntity<ApiResponseDto<EditApplicationResponseDto>> showApplication (
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();
        EditApplicationResponseDto result = memberService.showApplication(clubId, studentId);
        return ResponseEntity.ok(success(result));
    }


    @GetMapping("/mypage/edit")
    public ResponseEntity<ApiResponseDto<UpdateMyInfoResponseDto>> showMyInformation (
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();
        UpdateMyInfoResponseDto result = memberService.showMyInformation(studentId);

        return ResponseEntity.ok(success(result));
    }

    @PutMapping("/mypage/edit/email")
    public ResponseEntity<ApiResponseDto<UpdateMyEmailRequestDto>> editMyInformation(
            @RequestBody UpdateMyEmailRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();

        memberService.updateMyEmail(studentId, requestDto);

        return ResponseEntity.ok(success("이메일 수정 완료"));
    }

    @PutMapping("/mypage/edit/phone")
    public ResponseEntity<ApiResponseDto<UpdateMyEmailRequestDto>> editMyInformation(
            @RequestBody UpdateMyPhoneNumberRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();

        memberService.updateMyPhoneNumber(studentId, requestDto);

        return ResponseEntity.ok(success("전화번호 수정이 완료"));
    }

    @PostMapping("/mypage/application/{clubId}/delete")
    public ResponseEntity<ApiResponseDto<Void>> deleteApplication(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String studentId = userDetails.getUsername();

        memberService.deleteApplication(studentId, clubId);

        return ResponseEntity.ok(success("지원서 삭제가 완료"));
    }
}
