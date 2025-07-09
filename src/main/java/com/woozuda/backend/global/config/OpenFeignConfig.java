package com.woozuda.backend.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.woozuda.backend.question.service")
public class OpenFeignConfig {
}
