package com.example.smu_club.question.controller;

import com.example.smu_club.domain.Question;
import com.example.smu_club.question.dto.QuestionRequest;
import com.example.smu_club.question.dto.QuestionResponse;
import com.example.smu_club.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner/clubs")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/{clubId}/questions")
    public ResponseEntity<List<QuestionResponse>> getClubQuestions(@PathVariable Long clubId) {
        List<QuestionResponse> questions = questionService.findQuestionsByClubId(clubId);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/{clubId}/questions")
    public ResponseEntity<Void> saveClubQuestions(
            @PathVariable Long clubId,
            @RequestBody List<QuestionRequest> questions) {

        questionService.saveQuestions(clubId, questions);

        return ResponseEntity.ok().build();

    }
}
