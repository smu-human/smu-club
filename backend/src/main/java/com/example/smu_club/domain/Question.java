package com.example.smu_club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

//    @OneToMany(mappedBy ="question")
//    private List<Answer> answers = new ArrayList<>();

    private String content;

    @Column(name = "question_content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionContentType questionContentType;

    //질문 순서
    @Column(name = "order_num")
    private int orderNum;
}
