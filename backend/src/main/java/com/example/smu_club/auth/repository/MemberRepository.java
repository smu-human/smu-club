package com.example.smu_club.auth.repository;

import com.example.smu_club.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByStudentId(String studentId);

    Optional<Member> findByRefreshToken(String refreshToken);
}
