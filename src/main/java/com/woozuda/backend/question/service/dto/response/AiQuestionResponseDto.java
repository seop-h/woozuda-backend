package com.woozuda.backend.question.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AiQuestionResponseDto {

    private ApiStatus status;
    private ApiResult result;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ApiStatus {
        private String code;
        private String message;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ApiResult {
        private String text;
        private String stopReason;
        private Integer startLength;
        private String inputText;
        private Integer inputLength;
        private List<String> inputTokens;
        private String outputText;
        private Integer outputLength;
        private List<String> outputTokens;
        private List<Object> probs;
        private Boolean ok;
        private List<AiFilter> aiFilter;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class AiFilter {
        private String groupName;
        private String name;
        private String score;
    }

}
