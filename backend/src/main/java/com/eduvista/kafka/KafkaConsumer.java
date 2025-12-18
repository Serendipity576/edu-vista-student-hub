package com.eduvista.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConsumer {
    
    @KafkaListener(topics = "student-register", groupId = "student-hub-group")
    public void consumeStudentRegister(Map<String, Object> message, Acknowledgment ack) {
        try {
            log.info("收到学生注册消息: {}", message);
            
            String studentNo = (String) message.get("studentNo");
            String name = (String) message.get("name");
            String email = (String) message.get("email");
            
            sendWelcomeEmail(name, email);
            updateStudentPoints(studentNo, 100);
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理学生注册消息失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @KafkaListener(topics = "student-welcome", groupId = "student-hub-group")
    public void consumeWelcomeMessage(Map<String, Object> message, Acknowledgment ack) {
        try {
            log.info("收到欢迎消息: {}", message);
            String name = (String) message.get("name");
            log.info("欢迎 {} 加入系统！", name);
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理欢迎消息失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void sendWelcomeEmail(String name, String email) {
        log.info("发送欢迎邮件到: {}, 收件人: {}", email, name);
    }
    
    private void updateStudentPoints(String studentNo, int points) {
        log.info("为学生 {} 增加积分: {}", studentNo, points);
    }
}

