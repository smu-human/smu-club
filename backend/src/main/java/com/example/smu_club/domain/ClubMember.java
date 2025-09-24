package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubRole clubRole;

    private LocalDate appliedAt;


    @Enumerated(EnumType.STRING)
    private ClubMemberStatus status;

    private String memo;

    public ClubMember(Member member, Club club, ClubRole clubRole, LocalDate appliedAt, ClubMemberStatus status) {
        this.member = member;
        this.club = club;
        this.clubRole = clubRole;
        this.appliedAt = appliedAt;
        this.status = status;
    }
}
