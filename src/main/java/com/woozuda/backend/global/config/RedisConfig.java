package com.woozuda.backend.global.config;

import org.springframework.beans.factory.annotation.Value;

//@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /*
    Lettuce 라이브러리를 활용해 Redis 연결을 관리하는 객체를 생성하고
    Redis 서버에 대한 정보(host, port)를 설정한다.
     */
    /*
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }
    */

}
