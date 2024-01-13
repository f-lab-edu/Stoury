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
    public Object handleExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException e) {
            throw new MemberCreateException(e);
        } catch (Throwable e) {
            throw e;
        }
    }
}
