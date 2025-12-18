package com.eduvista.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String STUDENT_REGISTER_TOPIC = "student-register";
    private static final String STUDENT_WELCOME_TOPIC = "student-welcome";
    
    public void sendStudentRegisterMessage(String studentNo, String name, String email) {
        Map<String, Object> message = new HashMap<>();
        message.put("studentNo", studentNo);
        message.put("name", name);
        message.put("email", email);
        message.put("timestamp", System.currentTimeMillis());
        message.put("action", "register");
        
        try {
            kafkaTemplate.send(STUDENT_REGISTER_TOPIC, studentNo, message);
            log.info("发送学生注册消息成功: {}", studentNo);
        } catch (Exception e) {
            log.error("发送学生注册消息失败: {}", e.getMessage(), e);
        }
    }
    
    public void sendWelcomeMessage(String studentNo, String name) {
        Map<String, Object> message = new HashMap<>();
        message.put("studentNo", studentNo);
        message.put("name", name);
        message.put("message", "欢迎加入学生信息管理系统！");
        message.put("timestamp", System.currentTimeMillis());
        
        try {
            kafkaTemplate.send(STUDENT_WELCOME_TOPIC, studentNo, message);
            log.info("发送欢迎消息成功: {}", studentNo);
        } catch (Exception e) {
            log.error("发送欢迎消息失败: {}", e.getMessage(), e);
        }
    }
}

