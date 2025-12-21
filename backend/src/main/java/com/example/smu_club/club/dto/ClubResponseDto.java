package com.example.smu_club.club.dto;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.ClubImage;
import com.example.smu_club.domain.RecruitingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ClubResponseDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private RecruitingStatus recruitingStatus;
    private LocalDate recruitingStart;
    private LocalDate recruitingEnd;
    private String president;
    private String title;
    private String contact;
    private String clubRoom;
    private String thumbnailUrl;
    //toast UI
    private String description;
    //club Images
    private List<ClubImagesResponseDto> clubImages;
}
