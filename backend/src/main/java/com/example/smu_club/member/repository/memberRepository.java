package com.example.smu_club.member.repository;

import com.example.smu_club.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface memberRepository extends JpaRepository<Member, Long> {
}
