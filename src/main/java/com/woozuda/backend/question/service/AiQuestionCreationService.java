package com.woozuda.backend.question.service;

import com.woozuda.backend.question.entity.Question;
import com.woozuda.backend.question.repository.QuestionRepository;
import com.woozuda.backend.question.service.dto.request.AiQuestionRequestDto;
import com.woozuda.backend.question.service.dto.response.AiQuestionResponseDto;
import com.woozuda.backend.question.service.util.AiInputGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AiQuestionCreationService {

    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    private final QuestionRepository questionRepository;
    private final AiQuestionCreatorApiClient apiClient;

    @Value("${cloud.ncp.clova-studio.question-creator.api-key}")
    private String apiKey;

    @Value("${cloud.ncp.clova-studio.question-creator.request-id}")
    private String requestId;

    // 매일 자정 12시 00분 1초에 새로운 질문 생성
    @Scheduled(cron = "1 0 0 * * *")
    public void makeTodayAiQuestion() {
        AiQuestionRequestDto requestDto = AiQuestionRequestDto.of(AiInputGenerator.execute());
        log.info("[AI Question Creator] input={}", requestDto.getText());

        //AI 질문 생성기 API 호출
        AiQuestionResponseDto responseDto = apiClient.makeAiQuestion(
                AUTHORIZATION_PREFIX + apiKey,
                requestId,
                requestDto
        );

        if (hasError(responseDto)) {
            throw new IllegalArgumentException("API 요청에 실패했습니다");
        }

        String output = responseDto.getResult().getText();
        log.info("[AI Question Creator] output={}", output);

        //생성된 질문 저장
        questionRepository.save(Question.of(output));
    }

    //응답 상태 코드가 20000번대(성공)가 아니면 false 반환
    private boolean hasError(AiQuestionResponseDto response) {
        String code = response.getStatus().getCode();
        return !code.equals("20000") && !code.equals("20400");
    }

}
