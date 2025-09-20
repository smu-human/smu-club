package com.example.smu_club.club.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubMemberRepositoryLegarcy {

    @PersistenceContext
    private final EntityManager em;
    
    //clubMember의 Embedded Type인 ClubMemberId의 개수를 세어 Long type으로 반환하여 회원수를 반환하는 jpgl
    public Long countByClubId(Long clubId){
        return em.createQuery("select count(cm) from ClubMember cm where cm.clubMemberId.clubId = :clubId", Long.class)
                .setParameter("clubId", clubId)
                .getSingleResult();
    }
}
