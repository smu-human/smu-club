package com.example.smu_club.club.repository;

import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.ClubRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Long countByClubId(Long clubId);

    @Query("SELECT cm FROM ClubMember cm JOIN FETCH cm.club WHERE cm.member.id = :memberId AND cm.clubRole = :clubRole")
    List<ClubMember> findByMemberIdAndClubRoleWithClub(long memberId, ClubRole clubRole);

}
