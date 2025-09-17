package com.example.smu_club.club.repository;

import com.example.smu_club.domain.Club;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClubRepository {

    @PersistenceContext //Spring Entity Manager를 가져올 수 있게 해줌.
    private final EntityManager em;

    public Club findOne(Long id){
        return em.find(Club.class, id);
    }

    public List<Club> findAll(){
        return em.createQuery("select c from Club c", Club.class).getResultList();
    }


}