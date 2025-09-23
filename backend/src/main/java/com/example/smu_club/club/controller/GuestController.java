package com.example.smu_club.club.controller;

import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.club.dto.ClubResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import com.example.smu_club.club.service.ClubService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class GuestController {
    private final ClubService clubService;

    @GetMapping("/clubs")
    public ResponseEntity<ApiResponseDto<List<ClubsResponseDto>>> findAllClubs() {
        List<ClubsResponseDto> clubs = clubService.findAllClubs();

        return ResponseEntity.ok(ApiResponseDto.success(clubs, "전체 클럽 목록 조회 성공 [메인페이지]"));
    }

    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponseDto<ClubResponseDto>> findClubById(@PathVariable Long clubId){
        ClubResponseDto club = clubService.findClubById(clubId);

        return ResponseEntity.ok(ApiResponseDto.success(club, "클럽 상세 정보 조회 성공"));
    }


}

