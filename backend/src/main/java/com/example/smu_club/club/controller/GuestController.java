package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ApiResponseDto;
import com.example.smu_club.club.dto.ClubResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import com.example.smu_club.club.service.ClubService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class GuestController {
    private final ClubService clubService;

    @GetMapping("/clubs")
    public ApiResponseDto.ApiResponseToList<ClubsResponseDto> findAllClubs() {
        return new ApiResponseDto.ApiResponseToList<>(clubService.findAllClubs());
    }

    @GetMapping("/clubs/{clubId}")
    public ApiResponseDto.ApiResponse<ClubResponseDto> findClubById(@PathVariable Long clubId){
        return new ApiResponseDto.ApiResponse<>(clubService.findClubById(clubId));
    }


}

