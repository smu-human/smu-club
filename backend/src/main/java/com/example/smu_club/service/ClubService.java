package com.example.smu_club.service;

import com.example.smu_club.domain.Club;
import com.example.smu_club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //JPA 모든 데이터/로직 변경은 가급적 트랜잭션에서 실행 되어야함. -> 그래야 LAZY 로딩 같은 기능이 가능함
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;

    public Club findOne(Long ClubId){ return clubRepository.findOne(ClubId); }

    public List<Club> findClubs(){
        return clubRepository.findAll();
    }


}
