package com.example.smu_club.club.service;

import com.example.smu_club.answer.repository.AnswerRepository;
import com.example.smu_club.club.dto.ApplicationFormResponseDto;
import com.example.smu_club.club.dto.ClubResponseDto;
import com.example.smu_club.club.dto.ClubsResponseDto;
import com.example.smu_club.club.repository.ClubMemberRepository;
import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.Question;
import com.example.smu_club.exception.custom.ClubNotFoundException;
import com.example.smu_club.exception.custom.MemberNotFoundException;
import com.example.smu_club.member.repository.MemberRepository;
import com.example.smu_club.question.dto.QuestionResponse;
import com.example.smu_club.question.repository.ClubRepository;
import com.example.smu_club.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;


@Service
@Transactional(readOnly = true) //JPA 모든 데이터/로직 변경은 가급적 트랜잭션에서 실행 되어야함. -> 그래야 LAZY 로딩 같은 기능이 가능함
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final AnswerRepository answerRepository;
    public List<ClubsResponseDto> findAllClubs(){
        List<Club> findClubs = clubRepository.findAll();

        return findClubs.stream()
                .map(c -> new ClubsResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getRecruitingStatus(),
                        c.getCreatedAt()
                ))
                .collect(toList());
    }

    public ClubResponseDto findClubById(Long clubId){
        Club findClub = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("club id = "+ clubId +" is not found"));
        return new ClubResponseDto(findClub);
    }

    public ApplicationFormResponseDto findMemberInfoWithQuestions(Long clubId, UserDetails userDetails){
        /**
         * questions search
         */
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("club id = "+ clubId +" is not found"));

        List<Question> questionList =
                questionRepository.findAllByClubOrderByOrderNumAsc(club);

        List<QuestionResponse> clubQuestionListResponse =
                questionList.stream().map
                        (qr -> new QuestionResponse(
                                qr.getId(),
                                qr.getOrderNum(),
                                qr.getContent()
                        )).collect(toList());
        /**
         * Member search
         */
        String studentId = userDetails.getUsername();
        Member myInfo = memberRepository.findByStudentId(studentId).orElseThrow(() -> new MemberNotFoundException("student id = "+ studentId +" is not found"));
        /**
         * Transaction to Dto
         */
        return new ApplicationFormResponseDto(
                myInfo.getId(),
                myInfo.getStudentId(),
                myInfo.getName(),
                myInfo.getPhoneNumber(),
                clubQuestionListResponse
        );
    }

}


