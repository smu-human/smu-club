package com.example.smu_club.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubInfoRequest {

    private List<MultipartFile> clubImages;
    private String name;
    private String title;
    private String president;
    private String contact;
    private LocalDate recruitingEnd;
    private String clubRoom;
    private String description;

    // 기존에 있던 사진에 대해서는 프론트에서 이렇게 보내줌
    private List<String> remainingImageUrls;
    // 새로 추가할 사진들에 대해서는 새로 업로드한 파일
    private List<MultipartFile> newImages;
}
