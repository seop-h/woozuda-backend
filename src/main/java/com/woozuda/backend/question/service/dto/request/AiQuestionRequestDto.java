package com.woozuda.backend.question.service.dto.request;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class AiQuestionRequestDto {

    private final String text;

    private final Integer topK = 0;
    private final Float topP = 0.80f;
    private final Float repeatPenalty = 6.0f;
    private final List<String> stopBefore = Collections.emptyList();
    private final String restart = "";
    private final String start = "";
    private final Integer maxTokens = 128;
    private final Boolean includeTokens = true;
    private final Float temperature = 0.6f;
    private final Boolean includeAiFilters = false;

    public AiQuestionRequestDto(String text) {
        this.text = text;
    }

    public static AiQuestionRequestDto of(String text) {
        return new AiQuestionRequestDto(text);
    }

}
