package com.example.smu_club.controller;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.RecruitingStatus;
import com.example.smu_club.service.ClubService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class GuestController {
    private final ClubService clubService;

    @GetMapping("/api/v1/public/clubs")
    public ApiResponse findAllClubs() {
        // 1. 서비스로부터 엔티티 리스트를 받음
        List<Club> findClubs = clubService.findClubs();

        // 2. 컨트롤러에서 DTO로 변환 (변환의 책임이 컨트롤러로 이동)
        List<ClubResponseDto> result = findClubs.stream()
                .map(c -> new ClubResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRecruitingStatus(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<>(result.size(), result);
    }

    @Data
    @AllArgsConstructor
    static class ApiResponse<T>{
        private int clubCount; // 넣을까 말까?  -> 일단 확인용으로 넣어둠
        private T data;
    }

    @Data
    @AllArgsConstructor
    public static class ClubResponseDto{
        private Long id;
        private String name;
        private String description;
        private RecruitingStatus recruitingStatus;
        private LocalDateTime createdAt;

    }

}

