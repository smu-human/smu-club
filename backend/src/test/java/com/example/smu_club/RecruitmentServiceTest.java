package com.example.smu_club;

import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.OwnerClubService;
import com.example.smu_club.domain.*;
import com.example.smu_club.util.RecruitmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DataJpaTest
public class RecruitmentServiceTest {
    @Autowired
    RecruitmentService recruitmentService;
    @Autowired
    ClubRepository clubRepository;
    @Autowired
    ClubMemberRepository clubMemberRepository;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private OwnerClubService ownerClubService;

    @Test
    @DisplayName("모집 마감 대상 동아리 조회 테스트")
    public void testFindEndedClubs() {

        //given
        Club expiredClub = Club.builder()
                .name("Expired Club")
                .recruitingStatus(com.example.smu_club.domain.RecruitingStatus.OPEN)
                .recruitingEnd(java.time.LocalDate.now().minusDays(1))
                .build();

        Club activeClub = Club.builder()
                .name("Active Club")
                .recruitingStatus(com.example.smu_club.domain.RecruitingStatus.OPEN)
                .recruitingEnd(java.time.LocalDate.now().plusDays(1))
                .build();

        clubRepository.save(expiredClub);
        clubRepository.save(activeClub);


        //when
        List<RecruitmentService.ClosureTarget> endedClubs = recruitmentService.findEndedClubs(LocalDate.now());

        recruitmentService.closeRecruitments(endedClubs);

        //then

        Club updatedExpiredClub = clubRepository.findById(expiredClub.getId()).orElseThrow();
        Club updatedActiveClub = clubRepository.findById(activeClub.getId()).orElseThrow();

        assertThat(updatedExpiredClub.getRecruitingStatus()).isEqualTo(RecruitingStatus.CLOSED);
        assertThat(updatedActiveClub.getRecruitingStatus()).isEqualTo(RecruitingStatus.OPEN);
    }

    @Test
    @DisplayName("보류 중인 멤버를 조회하면 상태가 PROCESSING으로 변경되고 ID 리스트를 반환한다")
    void fetchPendingAndMarkAsProcessing_Success() {
        // given
        Long clubId = 1L;
        Club club = mock(Club.class);

        // 모집 종료 및 확정 상태라고 가정 (isClosedAndConfirmed가 true를 반환하도록 설정)
        //given(club.isClosed()).willReturn(true);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));

        ClubMember member1 = spy(ClubMember.builder().id(10L).emailStatus(EmailStatus.READY).build());
        ClubMember member2 = spy(ClubMember.builder().id(11L).emailStatus(EmailStatus.READY).build());
        List<ClubMember> targets = List.of(member1, member2);

        given(clubMemberRepository.findByClubAndEmailStatus(club, EmailStatus.READY)).willReturn(targets);

        // when
        List<Long> resultIds = ownerClubService.fetchPendingAndMarkAsProcessing(clubId);

        // then
        assertThat(resultIds).containsExactlyInAnyOrder(10L, 11L);
        assertThat(member1.getEmailStatus()).isEqualTo(EmailStatus.PROCESSING);
        assertThat(member2.getEmailStatus()).isEqualTo(EmailStatus.PROCESSING);

        // Dirty Checking에 의해 상태가 변경되었는지 확인
        verify(member1).setEmailStatus(EmailStatus.PROCESSING);
    }
}
