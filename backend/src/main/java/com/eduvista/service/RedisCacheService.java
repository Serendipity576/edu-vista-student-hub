package com.eduvista.service;

import com.eduvista.entity.Student;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    
    public void incrementOperationCount(String operation) {
        String key = "operation:count:" + operation;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }
    
    public Long getOperationCount(String operation) {
        String key = "operation:count:" + operation;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }
    
    public void cacheStudentHash(Student student) {
        String key = "student:hash:" + student.getId();
        redisTemplate.opsForHash().put(key, "id", student.getId());
        redisTemplate.opsForHash().put(key, "studentNo", student.getStudentNo());
        redisTemplate.opsForHash().put(key, "name", student.getName());
        redisTemplate.opsForHash().put(key, "gender", student.getGender() != null ? student.getGender() : "");
        redisTemplate.opsForHash().put(key, "phone", student.getPhone() != null ? student.getPhone() : "");
        redisTemplate.opsForHash().put(key, "email", student.getEmail() != null ? student.getEmail() : "");
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }
    
    public Student getStudentFromHash(Long studentId) {
        String key = "student:hash:" + studentId;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        // 简化版，实际应该映射所有字段
        Student student = new Student();
        student.setId(Long.parseLong(redisTemplate.opsForHash().get(key, "id").toString()));
        student.setStudentNo(redisTemplate.opsForHash().get(key, "studentNo").toString());
        student.setName(redisTemplate.opsForHash().get(key, "name").toString());
        return student;
    }
    
    public Student getStudentWithLock(Long studentId, StudentService studentService) {
        String lockKey = "lock:student:" + studentId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    Student student = getStudentFromHash(studentId);
                    if (student == null) {
                        student = studentService.findById(studentId);
                        cacheStudentHash(student);
                    }
                    return student;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return studentService.findById(studentId);
    }
}

