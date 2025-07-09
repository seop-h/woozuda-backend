package com.woozuda.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogExecutionTimeAspect {

    @Around("@annotation(com.woozuda.backend.aop.LogExecutionTime)")
    public void logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 실제 메서드 실행
        joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;
        log.info("[LogExecutionTime] {} executed in {} ms",
                joinPoint.getSignature(), executionTime);
    }
}
