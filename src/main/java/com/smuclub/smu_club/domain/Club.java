package com.smuclub.smu_club.domain;

import lombok.*;

import javax.persistence.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private long id;

    @OneToMany(mappedBy ="club")
    private List<ClubMember> clubMembers = new ArrayList<>();

    @OneToMany(mappedBy ="club")
    private List<Answer> answers = new ArrayList<>();

    @OneToOne(mappedBy ="club")
    private Question questions;

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


    public Club(){}
}
