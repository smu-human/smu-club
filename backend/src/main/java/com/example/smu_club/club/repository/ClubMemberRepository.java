package com.example.smu_club.club.repository;

import com.example.smu_club.domain.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Long countByClubId(Long clubId);

    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId")
    List<ClubMember> findAllWithMemberAndClubByStudentId(@Param("studentId") String studentId);

    @Query( "SELECT cm FROM ClubMember cm JOIN FETCH cm.member m JOIN FETCH cm.club c WHERE m.studentId = :studentId AND c.id = :clubId")
    ClubMember findAllWithMemberAndClubByStudentId(@Param("studentId") String studentId, @Param("clubId") Long clubId);

}
