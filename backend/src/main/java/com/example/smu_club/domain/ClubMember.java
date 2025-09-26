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
@Table(name = "club_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_club_member", columnNames = {"member_id", "club_id"})
        })
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


    private LocalDate appliedAt;

    @Enumerated(EnumType.STRING)
    private ClubMemberStatus status;

    private String memo;

    public ClubMember(Member member, Club club, LocalDate appliedAt, ClubMemberStatus status) {
        this.member = member;
        this.club = club;
        this.appliedAt = appliedAt;
        this.status = status;
    }
}
