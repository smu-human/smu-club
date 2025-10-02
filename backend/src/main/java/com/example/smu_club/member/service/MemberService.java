package com.example.smu_club.member.service;

import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.common.ApiResponseDto;
import com.example.smu_club.domain.ClubMember;
import com.example.smu_club.domain.Member;
import com.example.smu_club.exception.custom.ClubMemberNotFoundException;
import com.example.smu_club.member.dto.ApplicationListResponseDto;
import com.example.smu_club.member.dto.ApplicationResultResponseDto;
import com.example.smu_club.member.dto.EditApplicationResponseDto;
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
        ClubMember clubMember = clubMemberRepository.findAllWithMemberAndClubByStudentIdAndClubId(studentId, clubId);
        if(clubMember == null) throw new ClubMemberNotFoundException("해당 클럽에 지원하지 않았습니다.");

        return new ApplicationResultResponseDto(
                clubMember.getMember().getId(),
                clubMember.getClub().getId(),
                clubMember.getMember().getName(),
                clubMember.getClub().getName(),
                clubMember.getStatus()
        );

    }

//    public ApiResponseDto<EditApplicationResponseDto> showApplication(Long clubId, String studentId) {
//        /** 순서
//         *  1. studentId로 Member 객체를 생성하여 내 정보를 가져와야 됨.
//         *  2. clubId로 Question 객체를 List로 생성한다. -> orderNum으로 정렬한다.
//         *  3. Member 객체와 Question 객체를 이용하여 Answer객체를 List로 생성한다. (member_id, question_id) → UNIQUE  제약 조건
//         *  4. Map<Long, Answer> answerMap = answers.stream().collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> answer) f
//         *     를 이용하여 조회 성능을 향상 시키자.
//         *  5. EditApplicationResponseDto 안에 AnswerResponseDto 리스트를 채우기 위해 stream().map().collect()로 특정 필드를 채워 리스트를 생성한다.
//         *  5. EditApplicationResponseDto에 맞게 Member의 특정 필드와 와 AnswerResponseDto 리스트 값을 Mapping 후 반환한다.
//         */
//    }
}
