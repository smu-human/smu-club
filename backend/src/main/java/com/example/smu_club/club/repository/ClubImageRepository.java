package com.example.smu_club.club.repository;

import com.example.smu_club.domain.ClubImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubImageRepository extends JpaRepository<ClubImage, Long> {


    List<ClubImage> findAllByClubId(Long clubId);
}
