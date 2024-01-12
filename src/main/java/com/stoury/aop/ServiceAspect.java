package com.stoury.aop;

import com.stoury.exception.MemberCreateException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAspect {
    @Around("execution(* com.stoury.service.*Service.createMember(..))")
    public Object handleExceptions(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException | IllegalArgumentException e) {
            throw new MemberCreateException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
