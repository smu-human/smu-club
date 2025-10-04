package com.example.smu_club.club.service;


import com.example.smu_club.club.dto.ClubInfoRequest;
import com.example.smu_club.club.dto.ClubInfoResponse;
import com.example.smu_club.club.dto.ManagedClubResponse;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.club.repository.ClubRepository;
import com.example.smu_club.domain.*;
import com.example.smu_club.exception.custom.AuthorizationException;
import com.example.smu_club.exception.custom.ClubMemberNotFoundException;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.MemberNotFoundException;
import com.example.smu_club.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void register(String studentId, ClubInfoRequest request) {

        // 1. 클럽 정보 등록
        Club newClub = Club.builder()
                .name(request.getName())
                .title(request.getTitle())
                .description(request.getDescription())
                .president(request.getPresident())
                .contact(request.getContact())
                .clubRoom(request.getClubRoom())
                .recruitingEnd(request.getRecruitingEnd())
                .recruitingStart(null)
                .recruitingStatus(RecruitingStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        clubRepository.save(newClub);


        // 2. ClubMember 관계 만들어주기

        Member ownerMember = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("학번 :  " + studentId + "를 찾을 수 없습니다."));

        ClubMember clubMember = new ClubMember(
                ownerMember,
                newClub,
                ClubRole.OWNER,
                LocalDate.now(),
                ClubMemberStatus.ACCEPTED
        );

        clubMemberRepository.save(clubMember);
    }


    @Transactional(readOnly = true)
    public List<ManagedClubResponse> findManagedClubsByMemberId(String studentId) {

        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 해당 학번의 사용자를 찾을 수 없습니다: " + studentId));

        long memberId = member.getId();

        List<ClubMember> managedClubRelations = clubMemberRepository.findByMemberIdAndClubRoleWithClub(memberId, ClubRole.OWNER);

        return managedClubRelations.stream()
                .map(relation -> new ManagedClubResponse(
                        relation.getClub().getId(),
                        relation.getClub().getName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void startRecruitment(Long clubId, String studentId) {

        Member member = memberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new MemberNotFoundException("[OWNER] 힉번 : " + studentId + " 를 찾을 수 없습니다. "));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] ID: " + clubId + "인 동아리를 찾을 수 없습니다."));

        ClubMember clubMember = clubMemberRepository.findByClubAndMember(club, member)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException ("[OWNER] 동아리 모집을 시작할 권한이 없습니다. ");
        }

        club.startRecruitment();


    }

    @Transactional(readOnly = true)
    public ClubInfoResponse getClubInfo(Long clubId, String studentId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("[OWNER] 존재하지 않는 동아리입니다."));


        ClubMember clubMember = clubMemberRepository.findByClubAndMember_StudentId(club, studentId)
                .orElseThrow(() -> new ClubMemberNotFoundException("[OWNER] 해당 동아리 소속이 아니거나 존재하지 않는 회원입니다."));

        if (clubMember.getClubRole() != ClubRole.OWNER) {
            throw new AuthorizationException("[OWNER] 동아리를 조회할 권한이 없습니다.. ");
        }

        return ClubInfoResponse.from(club);



    }
}
