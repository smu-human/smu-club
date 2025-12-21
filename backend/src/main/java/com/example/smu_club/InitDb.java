package com.example.smu_club;

import com.example.smu_club.domain.*;
import com.example.smu_club.club.repository.ClubRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.smu_club.domain.ClubMemberStatus.PENDING;
import static com.example.smu_club.domain.QuestionContentType.TEXT;


@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;


//    @PostConstruct
//    public void init(){
//        initService.dbInit();
//    }
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{
        private final EntityManager em;
        private final ClubRepository clubRepository;
        public void dbInit(){
            //멤버 초기화
            int phoneNumber = 0;
            for(int i = 1 ; i <= 1000 ; i++){
                Member member = new Member();
                member.setName("user" + i);
                member.setEmail("user" + i + "@smu.ac.kr");
                member.setStudentId(String.valueOf(202215063+i));
                member.setDepartment("휴먼지능정보공학과");
                member.setRole(Role.MEMBER);
                member.setPhoneNumber(Integer.toString(phoneNumber));
                phoneNumber++;
                em.persist(member);
            }



            long memberIdCounter = 1L;

            //회원 모집중인 동아리 초기화
            for(int i = 0 ; i < 4 ; i++){
                Club club = new Club();
                createClub1(club);
                em.persist(club);
                //동아리당 100명씩 각자 다른사람으로 회원가입시킴.
                memberIdCounter = setMemberUsingIdCounter(memberIdCounter, club);
            }

            //회원 모집 예정인 동아리 초기화
            for(int i = 0 ; i < 3 ; i++){
                Club club = new Club();
                createClub2(club);
                em.persist(club);

                //동아리당 100명씩 각자 다른사람으로 회원가입시킴.
                memberIdCounter = setMemberUsingIdCounter(memberIdCounter, club);
            }

            //회원 모집중이지않은 동아리 초기화
            for(int i = 0 ; i < 3 ; i++){
                Club club = new Club();
                createClub3(club);
                em.persist(club);

                //동아리당 100명씩 각자 다른사람으로 회원가입시킴.
                memberIdCounter = setMemberUsingIdCounter(memberIdCounter, club);
            }

            //질문 추가
            createQuestion();
            em.flush();
            em.clear();

        }

        private void createQuestion() {
            Club findClub = clubRepository.findById(1L).get();

            Question q1 = new Question();
            q1.setOrderNum(1);
            q1.setContent("자기소개를 해보세요.");
            q1.setClub(findClub);
            q1.setQuestionContentType(TEXT);
            em.persist(q1);
            Question q2 = new Question();
            q2.setOrderNum(2);
            q2.setContent("지원 동기는 무엇인가요?");
            q2.setClub(findClub);
            q2.setQuestionContentType(TEXT);
            em.persist(q2);
            Question q3 = new Question();
            q3.setOrderNum(3);
            q3.setContent("사용 가능한 기술 스택을 알려주세요");
            q3.setClub(findClub);
            q3.setQuestionContentType(TEXT);
            em.persist(q3);
        }


        private long setMemberUsingIdCounter(long memberIdCounter, Club club) {
            for(int j = 0 ; j < 100 ; j++){
                Member member = em.find(Member.class, memberIdCounter);


                ClubMember clubMember = new ClubMember();
                clubMember.setClub(club);
                clubMember.setMember(member);
                clubMember.setClubRole(ClubRole.MEMBER);
                clubMember.setStatus(PENDING);
                em.persist(clubMember);

                memberIdCounter++;
            }
            return memberIdCounter;
        }

        //동아리 모집중 (OPEN)
        private void createClub1(Club club){
            club.setName("스뮤클럽");
            club.setTitle("안녕하세요 상명대학교 휴먼지능정보공학과에서 만든 스뮤클럽입니다.\n 저희 동아리는 개발동아리로 웹어플리케이션을 개발합니다.");
            club.setCreatedAt(LocalDateTime.now());
            club.setRecruitingStatus(RecruitingStatus.OPEN);
            club.setRecruitingStart(LocalDate.of(2025, 9, 1));
            club.setRecruitingEnd(LocalDate.of(2025, 9, 30));
            club.setPresident("유승준");
            club.setContact("010-4130-1904");
            club.setClubRoom("G308");
            club.setThumbnailFileKey(null);
        }

        //동아리 모집 예정 (UPCOMING)
        private void createClub2(Club club){
            club.setName("라면클럽");
            club.setTitle("안녕하세요 상명대학교 외식경영학과에서 만든 라면클럽입니다.\n 저희 동아리는 라면 조합을 연구합니다.");
            club.setCreatedAt(LocalDateTime.now());
            club.setRecruitingStatus(RecruitingStatus.UPCOMING);
            club.setRecruitingStart(LocalDate.of(2025, 10, 1));
            club.setRecruitingEnd(LocalDate.of(2025, 10, 30));
            club.setPresident("이윤표");
            club.setContact("010-1234-5678");
            club.setClubRoom("R301");
            club.setThumbnailFileKey(null);
        }

        //동아리 비 모집중 (CLOSED)
        private void createClub3(Club club){
            club.setName("헬스클럽");
            club.setTitle("안녕하세요 상명대학교 스포건강관리학과에서 만든 헬스클럽입니다.\n 저희 동아리는 헬스동아리로 3대 500에 도전합니다.");
            club.setCreatedAt(LocalDateTime.now());
            club.setRecruitingStatus(RecruitingStatus.CLOSED);
            club.setRecruitingStart(LocalDate.of(2025, 3, 1));
            club.setRecruitingEnd(LocalDate.of(2025, 3, 10));
            club.setPresident("차준규");
            club.setContact("010-9876-5432");
            club.setClubRoom("Y808");
            club.setThumbnailFileKey(null);
        }

    }
}

