package com.carsharing.carsharing.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.carsharing.carsharing.controller..*(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Executing method: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logger.info("Method executed: {}, Returned: {}", joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        logger.error("Exception in method: {}, Message: {}", joinPoint.getSignature().toShortString(), exception.getMessage());
    }
}