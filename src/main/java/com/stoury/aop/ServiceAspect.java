package com.stoury.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAspect {
    @Around("execution(* com.stoury.service.*Service.*(..))")
    public Object handleExceptions(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
