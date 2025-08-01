package com.smuclub.smu_club.domain;


import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private int id;

    @OneToOne
    @JoinColumn(name = "club_id")
    private Club club;

    @OneToMany(mappedBy ="question")
    private List<Answer> answers = new ArrayList<>();

    private String content;

    @Column(name = "question_content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionContentType questionContentType;

    @Column(name = "order_num")
    private int orderNum;

    public Question(){}
}
