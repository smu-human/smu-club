package com.example.smu_club.club.dto;


import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.Member;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ApplicantResponse {

    private final Long clubMemberId;
    private final Long memberId;
    private final String name;
    private final String studentId;
    private final LocalDate appliedAt;

    public static ApplicantResponse from(ClubMember clubMember) {
        Member member = clubMember.getMember();
        return new ApplicantResponse(
                clubMember.getId(),
                member.getId(),
                member.getName(),
                member.getStudentId(),
                clubMember.getAppliedAt()
        );
    }

    private ApplicantResponse(Long clubMemberId, Long memberId, String name, String studentId, LocalDate appliedAt) {
        this.clubMemberId = clubMemberId;
        this.memberId = memberId;
        this.name = name;
        this.studentId = studentId;
        this.appliedAt = appliedAt;
    }
}
