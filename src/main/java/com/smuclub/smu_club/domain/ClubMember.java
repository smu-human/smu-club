package com.smuclub.smu_club.domain;

import lombok.*;

import javax.persistence.*;
import java.time.*;

@Entity
@Getter
@Setter
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
