package com.example.smu_club.club.dto;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.ClubImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class ClubInfoResponse {

    private String name;
    private String title;
    private String president;
    private String contact;
    private LocalDate recruitingEnd;
    private String clubRoom;
    private String description;

    private List<String> clubImageUrls;

    public static ClubInfoResponse from(Club club, Function<String, String> urlConverter) {

        List<String> imageUrls = club.getClubImages().stream()
                .sorted(Comparator.comparingInt(ClubImage::getDisplayOrder))
                .map(image -> urlConverter.apply(image.getImageFileKey()))
                .toList();

        return ClubInfoResponse.builder()
                .name(club.getName())
                .title(club.getTitle())
                .president(club.getPresident())
                .contact(club.getContact())
                .recruitingEnd(club.getRecruitingEnd())
                .clubRoom(club.getClubRoom())
                .description(club.getDescription())
                .clubImageUrls(imageUrls)
                .build();
    }
}
