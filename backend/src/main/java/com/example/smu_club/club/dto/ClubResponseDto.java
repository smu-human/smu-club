package com.example.smu_club.club.dto;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.RecruitingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ClubResponseDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private RecruitingStatus recruitingStatus;
    private LocalDate recruitingStart;
    private LocalDate recruitingEnd;
    private String president;
    private String contact;
    private String clubRoom;
    private String thumbnailUrl;

    //객체를 파라미터로 넘겨 재사용성 증가
    public ClubResponseDto(Club club) {
        this.id = club.getId();
        this.name = club.getName();
        this.description = club.getDescription();
        this.createdAt = club.getCreatedAt();
        this.recruitingStatus = club.getRecruitingStatus();
        this.recruitingStart = club.getRecruitingStart();
        this.recruitingEnd = club.getRecruitingEnd();
        this.president = club.getPresident();
        this.contact = club.getContact();
        this.clubRoom = club.getClubRoom();
        this.thumbnailUrl = club.getThumbnailUrl();
    }
}
