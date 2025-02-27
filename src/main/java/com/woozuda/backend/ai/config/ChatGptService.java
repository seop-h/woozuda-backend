package com.woozuda.backend.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptService {
    /**
     * chat GPT 연동하는 서비스 로직 입니다.
     */

    private final WebClient webClient;

    @Value("${openai.api.url}")
    private String apiUrl;

    private static final int MAX_CONTEXT_TOKENS = 4096; // max 토큰 값 설정

    public String analyzeDiaryUsingGPT(String systemMessage, String userMessage) {
        int systemMessageTokens = estimateTokenCount(systemMessage);
        int remainingTokens = MAX_CONTEXT_TOKENS - systemMessageTokens;

        // userMessage의 토큰 수가 남은 토큰 수를 초과하지 않도록 계산
        int userMessageTokens = estimateTokenCount(userMessage);
        int maxTokens = remainingTokens - userMessageTokens;

        // maxTokens가 0보다 작지 않도록 확인
        maxTokens = Math.max(maxTokens, 50); // 최소 50 토큰은 남겨두기

        // 요청 데이터 구성
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo", // GPT 3.5 모델 사용
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                },
                "max_tokens", maxTokens // 응답에 사용할 최대 토큰 수 설정
        );

        // WebClient로 요청 보내기
        Mono<String> responseMono = webClient.post()
                .uri(apiUrl)  // 실제 엔드포인트 경로로 수정
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        // 동기적으로 응답을 기다리기 위해 block() 사용
        return responseMono.block();
    }

    private int estimateTokenCount(String text) {
        // 간단한 토큰 계산: 한글 기준으로 1.5~2배로 계산
        return text.split("\\s+").length * 2; // 한 단어에 대해 2토큰으로 계산
    }

}



