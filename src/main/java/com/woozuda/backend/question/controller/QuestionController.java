package com.woozuda.backend.question.controller;

import com.woozuda.backend.question.dto.response.QuestionResponseDto;
import com.woozuda.backend.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<QuestionResponseDto> getTodayQuestion() {
        QuestionResponseDto responseDto = questionService.getTodayQuestion();
        return ResponseEntity.ok(responseDto);
    }
}
