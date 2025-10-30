package com.example.smu_club.club.repository;

import com.example.smu_club.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {


    @Query("SELECT c FROM Club c ORDER BY c.recruitPriority ASC, c.name ASC")
    List<Club> findAllSortedByRecruitment();

}

