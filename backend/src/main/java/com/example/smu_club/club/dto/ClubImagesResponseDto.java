package com.example.smu_club.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubImagesResponseDto {
    private Long id;
    private String imageUrl;
    private int orderNumber;
}
