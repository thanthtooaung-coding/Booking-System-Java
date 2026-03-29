package com.alvin.bookingsystem.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(public * com.alvin.bookingsystem.controller..*(..))")
    public Object logApiExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        log.info("[API] {} executed in {} ms", joinPoint.getSignature(), executionTime);
        return proceed;
    }

    @Pointcut("execution(* com.alvin.bookingsystem.service.impl..*(..))")
    public void serviceLayerMethods() {
    }

    @Before("serviceLayerMethods()")
    public void beforeServiceMethod(JoinPoint joinPoint) {
        log.info("[Service] Before executing: {}", joinPoint.getSignature());
    }

    @AfterReturning(pointcut = "serviceLayerMethods()")
    public void afterReturningServiceMethod(JoinPoint joinPoint) {
        log.info("[Service] After executing: {}", joinPoint.getSignature());
    }

    @AfterThrowing(pointcut = "serviceLayerMethods()", throwing = "exception")
    public void afterThrowingServiceMethod(JoinPoint joinPoint, Exception exception) {
        log.error("[Service] Exception in: {} | Message: {}", joinPoint.getSignature(), exception.getMessage(), exception);
    }
}
