package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private long id;

    @Column(nullable = false)
    private String name;

    // 대표 메인화면에 표시되면 동아리 설명에 해당합니다.
    private String title;

    @Lob // Toast UI 에디터 내용은 길이를 예측할 수 없으므로 @Lob 추가 (TEXT 타입)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "recruiting_status")
    @Enumerated(EnumType.STRING) //ORDINARY 사용 금지
    private RecruitingStatus recruitingStatus;

    @Column(name = "recruiting_start")
    private LocalDate recruitingStart;

    @Column(name = "recruiting_end")
    private LocalDate recruitingEnd;

    private String president;

    private String contact;

    private String clubRoom;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}
