package com.example.smu_club.club.repository;

import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.ClubMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {
    Long countByClubId(Long clubId);

}
