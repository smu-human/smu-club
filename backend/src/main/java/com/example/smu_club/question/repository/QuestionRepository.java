package com.example.smu_club.question.repository;

import com.example.smu_club.domain.Club;
import com.example.smu_club.domain.Question;
import com.example.smu_club.domain.QuestionContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findAllByClubOrderByOrderNumAsc(Club club);

    void deleteAllByClubAndQuestionContentType(Club club, QuestionContentType questionContentType);
}
