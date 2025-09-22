package com.example.smu_club.club.dto;

import com.example.smu_club.domain.RecruitingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClubsResponseDto {
    private Long id;
    private String name;
    private String description;
    private RecruitingStatus recruitingStatus;
    private LocalDateTime createdAt;
}
