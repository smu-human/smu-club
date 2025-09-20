package com.example.smu_club.club.controller;


import com.example.smu_club.club.dto.ApiResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/api/v1//api/v1/member/clubs/{clubId}/apply/clubs")
    public ApiResponseDto.ClubResponseDto getMemberInfoForApplication (@PathVariable int clubId) {
        return new Api
    }
}
