package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ClubGuestDto;
import com.example.smu_club.club.service.ClubService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class GuestController {
    private final ClubService clubService;

    @GetMapping("/api/v1/public/clubs")
    public ApiResponseToList<ClubGuestDto.ClubsResponseDto> findAllClubs() {
        return new ApiResponseToList<>(clubService.findAllClubs());
    }

    @GetMapping("/api/v1/public/clubs/{clubId}")
    public ApiResponse<ClubGuestDto.ClubResponseDto> findClubById(@PathVariable Long clubId){
        return new ApiResponse<>(clubService.findClubById(clubId));
    }

    @Data
    @AllArgsConstructor
    public static class ApiResponseToList<T>{
        private List<T> data;
    }
    @Data
    @AllArgsConstructor
    public static class ApiResponse<T>{
        private T data;
    }
}

