package com.example.smu_club.club.controller;


import com.example.smu_club.answer.dto.AnswerRequestDto;
import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import com.example.smu_club.club.dto.ApplicationRequestDto;
import com.example.smu_club.club.dto.ApplicationResponseDto;
import com.example.smu_club.club.service.MemberClubService;
import com.example.smu_club.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author sjy
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberClubController {

    private final MemberClubService memberClubService;

    @GetMapping("/clubs/{clubId}/apply")
    public ResponseEntity<ApiResponseDto<ApplicationFormResponseDto>> getApplication(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String studentId = userDetails.getUsername();
        ApplicationFormResponseDto responseDto = memberClubService.findMemberInfoWithQuestions(clubId, studentId);

        ApiResponseDto<ApplicationFormResponseDto> apiResponseDto = ApiResponseDto.success(
                responseDto,
                "동아리 지원서 조회에 성공했습니다."
        );

        return ResponseEntity.ok(apiResponseDto);
    }


    @PostMapping("/clubs/{clubId}/apply")
    public ResponseEntity<ApiResponseDto<ApplicationResponseDto>> createApplication(
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ApplicationRequestDto applicationRequestDto
    ) {
        String studentId = userDetails.getUsername();
        List<AnswerRequestDto> ard = applicationRequestDto.getQuestionAndAnswer();
        String fileUrl = applicationRequestDto.getFileUrl();

        ApplicationResponseDto responseDto = memberClubService.saveApplication(clubId, studentId, ard, fileUrl);
        ApiResponseDto<ApplicationResponseDto> apiResponseDto = ApiResponseDto.success(
                responseDto,
                "지원서 제출에 성공했습니다."
        );

        return ResponseEntity.ok(apiResponseDto);
    }




}
