package com.example.smu_club.member.dto;

import com.example.smu_club.answer.dto.AnswerResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EditApplicationResponseDto {
    //내정보
    private Long memberId;
    private String studentId;
    private String name;
    private String phone;

    //질문, 답변, 파일
    private final List<AnswerResponseDto> QuestionAndAnswer;
    private final String fileUrl;
}
