package com.example.smu_club.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClubInfoRequest {

    private String thumbnailUrl;
    private String name;
    private String title;
    private String president;
    private String contact;
    private LocalDate recruitingEnd;
    private String clubRoom;
    private String description;
}
