package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class ClubMember {

    @EmbeddedId
    private ClubMemberId clubMemberId;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("clubId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;


    private LocalDate appliedAt;

    @Enumerated(EnumType.STRING)
    private ClubMemberStatus status;

    private String memo;

    public ClubMember(){}
}
