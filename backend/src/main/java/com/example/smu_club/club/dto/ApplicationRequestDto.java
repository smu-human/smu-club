package com.example.smu_club.club.dto;

import com.example.smu_club.answer.dto.AnswerRequestDto;
import lombok.*;

import java.util.List;

@Data
public class ApplicationRequestDto {
    private List<AnswerRequestDto> qna;
    private String fileUrl;
}
