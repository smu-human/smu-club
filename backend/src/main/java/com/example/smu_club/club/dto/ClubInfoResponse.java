package com.example.smu_club.club.dto;

import com.example.smu_club.domain.Club;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ClubInfoResponse {

    private String thumbnailUrl;
    private String name;
    private String title;
    private String president;
    private String contact;
    private LocalDate recruitingEnd;
    private String clubRoom;
    private String description;

    public static ClubInfoResponse from(Club club) {
        return new ClubInfoResponse(
                club.getThumbnailUrl(),
                club.getName(),
                club.getTitle(),
                club.getPresident(),
                club.getContact(),
                club.getRecruitingEnd(),
                club.getClubRoom(),
                club.getDescription()
        );
    }
}
