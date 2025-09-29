package com.example.smu_club.member.service;

import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.Member;
import com.example.smu_club.exception.custom.ClubMemberNotFoundException;
import com.example.smu_club.member.dto.ApplicationListResponseDto;
import com.example.smu_club.member.dto.ApplicationResultResponseDto;
import com.example.smu_club.member.dto.MemberNameResponseDto;
import com.example.smu_club.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //JPA 모든 데이터/로직 변경은 가급적 트랜잭션에서 실행 되어야함. -> 그래야 LAZY 로딩 같은 기능이 가능함
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final ClubMemberRepository clubMemberRepository;


    public MemberNameResponseDto MyName(String studentId) {
        Member member = memberRepository.findByStudentId(studentId).get();
        return new MemberNameResponseDto(member.getName());
    }

    public List<ApplicationListResponseDto> findApplications(String studentId) {
        //여기서 나올 수 있는 예외가 있을까? -> 토큰에서 넘겨준 studentId가 잘못된 경우 밖에 없을 것이다.

        //1. 내가 가진 정보는 학번 뿐이다. -> 넘겨줄 데이터는 클럽의 아이디와 클럽의 이름
        // [ClubMember에서 Member엔티티 중 학번과 일치하는 Member 엔티티만 조회함.]
        List<ClubMember> clubMember = clubMemberRepository.findAllWithMemberAndClubByStudentId(studentId);



        //3.필요한 ClubMember만 List로 저장되었으니 Dto로 변환한다.
        //중요한점은 아무것도 없는 null 인 List여도 상관없다는 것이다.
        return clubMember.stream().map(
                cm -> new ApplicationListResponseDto(
                        cm.getId(),
                        cm.getClub().getName()
                )).toList();
    }

    public ApplicationResultResponseDto findResult(String studentId, Long clubId) {
        ClubMember clubMember = clubMemberRepository.findAllWithMemberAndClubByStudentId(studentId, clubId);
        if(clubMember == null) throw new ClubMemberNotFoundException("해당 클럽에 지원하지 않았습니다.");

        return new ApplicationResultResponseDto(
                clubMember.getMember().getId(),
                clubMember.getClub().getId(),
                clubMember.getStatus()
        );

    }
}
