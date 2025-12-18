package com.eduvista.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j // 使用 Lombok 的日志注解，如果没有安装请使用传统的 Logger 定义
public class WebLogAspect {

    // 切入点：匹配 com.eduvista.controller 包及其子包下的所有类的所有方法
    @Pointcut("execution(* com.eduvista.controller..*.*(..))")
    public void webLog() {}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 打印请求详细信息
            log.info("======= 收到新的 HTTP 请求 =======");
            log.info("URL    : {}", request.getRequestURL().toString());
            log.info("HTTP方法: {}", request.getMethod());
            log.info("IP地址  : {}", request.getRemoteAddr());
            log.info("处理器  : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        }
    }
}