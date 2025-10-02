package com.example.smu_club.club.service;


import com.example.smu_club.club.dto.ClubResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.Club;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.ClubsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class GuestClubService {

    private final ClubRepository clubRepository;


    @Transactional
    public List<ClubsResponseDto> findAllClubs(){

        List<Club> findClubs = clubRepository.findAll();
        if (findClubs.isEmpty())
            throw new ClubsNotFoundException("등록된 클럽이 하나도 없습니다.");

        return findClubs.stream()
                .map(c -> new ClubsResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getTitle(),
                        c.getRecruitingStatus(),
                        c.getCreatedAt()
                ))
                .collect(toList());
    }


    @Transactional
    public ClubResponseDto findClubById(Long clubId){
        Club findClub = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("club id = "+ clubId +" is not found"));
        return new ClubResponseDto(findClub);
    }
}
