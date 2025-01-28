package com.example.bookstore.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect
{
    @Pointcut("execution(* com.example.bookstore.service.*.*(..))")
    public void servicePackagePointcut() {}

    @Before("servicePackagePointcut()")
    public void beforeMethod()
    {
        log.info("Before method execution");
    }

    @After("servicePackagePointcut()")
    public void afterMethod()
    {
        log.info("After method execution");
    }

    @Around("servicePackagePointcut()")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        log.info("Method {} is about to be executed",joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log.info("Method {} executed in {}ms",joinPoint.getSignature().getName(),(endTime - startTime));
        return result;
    }

    @AfterReturning(pointcut = "servicePackagePointcut()", returning = "result")
    public void afterReturning(Object result)
    {
        log.info("Method successfully returned: {}", result);
    }

    @AfterThrowing(pointcut = "servicePackagePointcut()", throwing = "exception")
    public void afterThrowing(Exception exception)
    {
        log.error("Method threw exception: {}", exception.getMessage());
    }
}
