package com.woozuda.backend.question.service;

import com.woozuda.backend.question.dto.response.QuestionResponseDto;
import com.woozuda.backend.question.entity.Question;
import com.woozuda.backend.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public QuestionResponseDto getTodayQuestion() {
        LocalDate today = LocalDate.now();

        //TODO 오늘의 질문이 없는 경우 에러 커스텀
        Question question = questionRepository.findByTodayDate(today);
        if (question == null) {
            throw new RuntimeException("오늘의 질문이 없습니다.");
        }

        return QuestionResponseDto.from(question);
    }
}
