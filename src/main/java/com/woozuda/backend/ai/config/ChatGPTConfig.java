package com.woozuda.backend.ai.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ChatGPT API 통합을 위한 설정 클래스입니다.
 * RestTemplate 및 HTTP 헤더 관련 설정
 */
@Configuration
@Slf4j
public class ChatGPTConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;


    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)  // API base URL 설정
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)  // Authorization 헤더 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)  // Content-Type 설정
                .build();
    }

}