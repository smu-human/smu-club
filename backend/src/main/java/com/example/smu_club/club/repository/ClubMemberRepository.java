package com.example.smu_club.club.repository;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.ClubRole;
import com.example.smu_club.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Long countByClubId(Long clubId);

    @Query("SELECT cm FROM ClubMember cm JOIN FETCH cm.club WHERE cm.member.id = :memberId AND cm.clubRole = :clubRole")
    List<ClubMember> findByMemberIdAndClubRoleWithClub(long memberId, ClubRole clubRole);
  
    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId")
    List<ClubMember> findAllWithMemberAndClubByStudentId(@Param("studentId") String studentId);

    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId AND c.id = :clubId")
    ClubMember findAllWithMemberAndClubByStudentIdAndClubId(@Param("studentId") String studentId, @Param("clubId") Long clubId);


    Optional<ClubMember> findByClubAndMember(Club club, Member member);

    Optional<ClubMember> findByClubAndMember_StudentId(Club club, String studentId);
}
