package com.woozuda.backend.global.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HikariSettingsLogger implements CommandLineRunner {

    private final HikariDataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("maxLifetime: {}", dataSource.getMaxLifetime());
        log.info("idleTimeout: {}", dataSource.getIdleTimeout());
        log.info("maximumPoolSize: {}", dataSource.getMaximumPoolSize());
        log.info("minimumIdle: {}", dataSource.getMinimumIdle());
        log.info("connectionTimeout: {}", dataSource.getConnectionTimeout());
    }
}
