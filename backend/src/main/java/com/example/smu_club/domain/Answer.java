package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @EmbeddedId
    @Column(name = "answer_id")
    private AnswerId answerId;

    //Answer 입장 : 하나의 Answer는 하나의 Member/Club/Question에 속한다
    //Member/Club/Question 입장 : 하나의 Member/Club/Question는 여러 Answer에 속한다.
    //고로 @ManyToOne을 사용한다.
    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("clubId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @MapsId("questionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "answer_content")
    private String answerContent; //텍스트 답변

    @Column(name = "file_url")
    private String fileUrl; //S3 매핑 주소

}
