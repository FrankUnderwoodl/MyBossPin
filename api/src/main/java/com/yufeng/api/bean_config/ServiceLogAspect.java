package com.yufeng.api.bean_config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author Lzm
 * @CreateTime 2025年7月15日 20:33
 * @describe 日志切面，用来记录每一个微服务里面的service所执行的时间
 */
@Slf4j
@Component
@Aspect
public class ServiceLogAspect {


    /**
     * 记录每一个impl的service所执行的时间
     *
     * @param joinPoint 切入点
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     * '@Around注解表示这是一个环绕通知，它会在方法执行前后执行
     * 在环绕通知中，我们可以获取方法执行的开始时间和结束时间
     */
    @SuppressWarnings("AopLanguageInspection")
    @Around("execution(* com.yufeng.service.impl..*.*(..))") // 匹配com.yufeng.service.impl包及其子包下的所有方法
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取方法执行开始时间
        long startTime = System.currentTimeMillis();

        // 获取秒表，用于记录方法执行时间
        StopWatch stopWatch = new StopWatch();

        // 执行目标方法，因为要计算执行时间，所以使用proceed方法，并且放在中间通知中执行
        Object proceed = joinPoint.proceed();

        // 获取方法执行结束时间
        long endTime = System.currentTimeMillis();

        // 计算执行时间，并记录日志
        long takeTime = endTime - startTime;

        // 如果执行时间超过3000毫秒，记录警告日志
        if (takeTime > 3000) {
            log.warn("接口 {} 执行时间过长: {} 毫秒**", joinPoint.getSignature().toShortString(), takeTime);
            log.info("==========================================");
        } else {

            log.info("接口 {} 执行时间: {} 毫秒**", joinPoint.getSignature().toShortString(), takeTime);
            log.info("==========================================");
        }

        return proceed;
    }

}
