package com.example.smu_club.answer.repository;

import com.example.smu_club.domain.Answer;
import com.example.smu_club.domain.Member;
import com.example.smu_club.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM Answer a WHERE a.member = :member AND a.question IN :questions")
    List<Answer> findByMemberAndQuestions(@Param("member")Member member, @Param("questions") List<Question> questions);

    @Query("DELETE FROM Answer a WHERE a.member = :member AND a.question IN :questions")
    void deleteByMemberAndQuestionId(Member member, List<Question> questions);
}
