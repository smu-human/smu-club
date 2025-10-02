package com.example.smu_club.club.controller;

import java.util.List;
import com.example.smu_club.answer.dto.AnswerRequestDto;
import com.example.smu_club.club.dto.*;
import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    /**
     *
     * GUEST 권한
     */
    @GetMapping("/api/v1/public/clubs")
    public ResponseEntity<ApiResponseDto<List<ClubsResponseDto>>> findAllClubs() {
        List<ClubsResponseDto> clubs = clubService.findAllClubs();

        return ResponseEntity.ok(ApiResponseDto.success(clubs, "전체 클럽 목록 조회 성공 [메인페이지]"));
    }

    @GetMapping("/api/v1/public/clubs/{clubId}")
    public ResponseEntity<ApiResponseDto<ClubResponseDto>> findClubById(@PathVariable Long clubId){
        ClubResponseDto club = clubService.findClubById(clubId);

        return ResponseEntity.ok(ApiResponseDto.success(club, "클럽 상세 정보 조회 성공"));
    }



    /**
     *
     * MEMBER 권한
     */
    @GetMapping("/api/v1/member/clubs/{clubId}/apply")
    public ResponseEntity<ApiResponseDto<ApplicationFormResponseDto>> getQMyInfoAndQuestions (
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails
            ){

        ApplicationFormResponseDto responseDto = clubService.findMemberInfoWithQuestions(clubId, userDetails);

        ApiResponseDto<ApplicationFormResponseDto> apiResponseDto = ApiResponseDto.success(
                responseDto,
                "동아리 지원서 조회에 성공했습니다. "
        );

        return ResponseEntity.ok(apiResponseDto);
    }

    @PostMapping("/api/v1/member/clubs/{clubId}/apply")
    public ResponseEntity<ApiResponseDto<ApplicationResponseDto>> createApplication (
            @PathVariable Long clubId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ApplicationRequestDto applicationRequestDto
            ){
        String studentId = userDetails.getUsername();
        List<AnswerRequestDto> ard = applicationRequestDto.getQuestionAndAnswer();
        String fileUrl = applicationRequestDto.getFileUrl();

        ApplicationResponseDto responseDto = clubService.saveApplication(clubId, studentId, ard, fileUrl);
        ApiResponseDto<ApplicationResponseDto> apiResponseDto = ApiResponseDto.success(
                responseDto,
                "지원서 제출에 성공했습니다."
        );

        return ResponseEntity.ok(apiResponseDto);
    }
}
