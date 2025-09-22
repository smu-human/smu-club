package com.example.smu_club.club.repository;

import com.example.smu_club.domain.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Long countByClubId(Long clubId);

}
