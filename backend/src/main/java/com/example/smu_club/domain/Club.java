package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
