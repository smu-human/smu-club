package com.example.smu_club.question.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QuestionResponse {

    private final Long questionId;
    private final String content;
    private final int orderNum;
}
