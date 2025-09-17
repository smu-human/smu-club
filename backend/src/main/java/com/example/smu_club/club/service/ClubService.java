package com.example.smu_club.club.service;

import com.example.smu_club.club.dto.ClubGuestDto;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true) //JPA 모든 데이터/로직 변경은 가급적 트랜잭션에서 실행 되어야함. -> 그래야 LAZY 로딩 같은 기능이 가능함
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;

    public List<ClubGuestDto.ClubsResponseDto> findAllClubs(){
        List<Club> findClubs = clubRepository.findAll();

        return findClubs.stream()
                .map(c -> new ClubGuestDto.ClubsResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRecruitingStatus(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public ClubGuestDto.ClubResponseDto findClubById(Long id){
        Club findClub = clubRepository.findOne(id);
        if(findClub == null) throw new IllegalArgumentException("id = " + id + " 해당 클럽을 찾을 수 없습니다.");
        return new ClubGuestDto.ClubResponseDto(findClub);
    }
}


