package com.woozuda.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Spring Scheduler 는 기본적으로 싱글 스레드도 동작.
 * 그러나 이 애플리케이션에서 사용하는 스케줄러는 현재 두 개
 *  : AiQuestionCreationService.makeTodayAiQuestion(), AlarmService.sendHeartbeat()
 * 확률이 매우 낮긴 하지만 이 두 스케줄러가 동시에 실행될 때, 싱글 스레드 환경이라면 예상과는 다르게 동작할 수도 있음.
 * 따라서 스케줄링 작업에 할당할 스레드를 3개로 지정해, 하나의 스케줄러가 다른 것에 영향을 미치치 않도록 멀티 스레드 환경을 구축
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        threadPool.setPoolSize(3);
        threadPool.setThreadNamePrefix("scheduler-task");
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool);
    }
}
