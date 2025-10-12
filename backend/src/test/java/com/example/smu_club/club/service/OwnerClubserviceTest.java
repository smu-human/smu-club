package com.example.smu_club.club.service;

import com.example.smu_club.club.dto.ApplicantResponse;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.club.service.MemberClubService;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.AuthorizationException;
import com.example.smu_club.exception.custom.ClubMemberNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OwnerClubserviceTest {

    @InjectMocks
    private MemberClubService memberClubService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMemberRepository clubMemberRepository;

    @Test
    @DisplayName("지원자 목록 조회 성공")
    void getApplicantList_success() {
        // given
        Long clubId = 1L;
        String ownerStudentId = "owner123";

        Club club = Club.builder().id(clubId).name("테스트 동아리").build();
        Member ownerMemberInfo = Member.builder().studentId(ownerStudentId).build();

        ClubMember owner = ClubMember.builder().club(club).member(ownerMemberInfo).clubRole(ClubRole.OWNER).build();

        // 지원자 목록 (PENDING 상태)
        List<ClubMember> applicants = List.of(
                ClubMember.builder().id(10L).member(Member.builder().id(100L).name("지원자A").studentId("app1").build()).appliedAt(LocalDate.now()).build(),
                ClubMember.builder().id(11L).member(Member.builder().id(101L).name("지원자B").studentId("app2").build()).appliedAt(LocalDate.now()).build()
        );

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubMemberRepository.findByClubAndMember_StudentId(club, ownerStudentId)).willReturn(Optional.of(owner));
        given(clubMemberRepository.findByClubAndStatus(club, ClubMemberStatus.PENDING)).willReturn(applicants);

        // when
        List<ApplicantResponse> result = memberClubService.getApplicantList(clubId, ownerStudentId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStudentId()).isEqualTo("app1");
        assertThat(result.get(1).getName()).isEqualTo("지원자B");

        verify(clubRepository).findById(clubId);
        verify(clubMemberRepository).findByClubAndMember_StudentId(club, ownerStudentId);
        verify(clubMemberRepository).findByClubAndStatus(club, ClubMemberStatus.PENDING);
    }

    @Test
    @DisplayName("지원자 목록 조회 실패 - 권한 없음 (OWNER가 아님)")
    void getApplicantList_fail_notOwner() {
        // given
        Long clubId = 1L;
        String memberStudentId = "member123";
        Club club = Club.builder().id(clubId).build();
        Member memberInfo = Member.builder().studentId(memberStudentId).build();

        ClubMember normalMember = ClubMember.builder().club(club).member(memberInfo).clubRole(ClubRole.MEMBER).build(); // 일반 멤버

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubMemberRepository.findByClubAndMember_StudentId(club, memberStudentId)).willReturn(Optional.of(normalMember));

        // when & then
        assertThrows(AuthorizationException.class, () -> {
            memberClubService.getApplicantList(clubId, memberStudentId);
        });
    }

    // 추가로 ClubNotFound, ClubMemberNotFound 예외 케이스도 위와 같은 방식으로 작성할 수 있습니다.
}