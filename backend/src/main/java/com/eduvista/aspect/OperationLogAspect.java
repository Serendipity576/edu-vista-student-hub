package com.eduvista.aspect;

import com.eduvista.entity.OperationLog;
import com.eduvista.repository.OperationLogRepository;
import com.eduvista.service.RedisCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final OperationLogRepository operationLogRepository;
    private final RedisCacheService redisCacheService;
    
    @Pointcut("execution(* com.eduvista.controller..*.*(..))")
    public void controllerPointcut() {}
    
    @Around("controllerPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = "anonymous";
        
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception e) {
            log.warn("获取用户名失败", e);
        }
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String operation = joinPoint.getSignature().getName();
        String method = request.getMethod();
        String url = request.getRequestURI();
        String ip = getClientIp(request);
        String params = Arrays.toString(joinPoint.getArgs());
        
        Object result = null;
        
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            OperationLog operationLog = OperationLog.builder()
                .username(username)
                .operation(operation)
                .method(method)
                .url(url)
                .ip(ip)
                .params(params.length() > 1000 ? params.substring(0, 1000) : params)
                .executionTime(executionTime)
                .build();
            
            operationLogRepository.save(operationLog);
            
            try {
                redisCacheService.incrementOperationCount(operation);
            } catch (Exception e) {
                // Redis 不可用时，静默失败，不影响主流程
                log.debug("Redis 操作失败，已跳过: {}", e.getMessage());
            }
        }
        
        return result;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

