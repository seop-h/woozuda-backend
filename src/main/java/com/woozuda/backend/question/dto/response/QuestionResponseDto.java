package com.woozuda.backend.question.dto.response;

import com.woozuda.backend.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QuestionResponseDto {

    private String question;

    public static QuestionResponseDto from(Question question) {
        return new QuestionResponseDto(question.getContent());
    }

}
