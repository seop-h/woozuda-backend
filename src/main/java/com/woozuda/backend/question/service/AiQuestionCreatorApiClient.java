package com.woozuda.backend.question.service;

import com.woozuda.backend.question.service.dto.request.AiQuestionRequestDto;
import com.woozuda.backend.question.service.dto.response.AiQuestionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "AiQuestionCreatorApiClient", url = "${cloud.ncp.clova-studio.question-creator.url}")
public interface AiQuestionCreatorApiClient {

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    AiQuestionResponseDto makeAiQuestion(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-NCP-CLOVASTUDIO-REQUEST-ID") String requestId,
            @RequestBody AiQuestionRequestDto requestDto
    );

}
