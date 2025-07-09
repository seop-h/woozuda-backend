package com.woozuda.backend.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
@Slf4j
public class NaverCLOVAConfig {

    @Value("${clova.api.key}")
    private String apiKey;


    @Value("${clova.api.url}")
    private String apiUrl;


    @Value("${clova.api.rid}")
    private String apiRid;

    @Bean(name = "clovaWebClient")
    public WebClient webClient() {
        return WebClient.builder().build();
    }



    public String analyzeDiaryUsingCLOVA(String systemMessage,String userMessage){
        Map<String, Object> requestBody = Map.of(
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                }

        );

        Mono<String> responseMono = webClient().post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", apiRid)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        // 동기적으로 응답을 기다리기 위해 block() 사용
            return responseMono.block();
        }


}
