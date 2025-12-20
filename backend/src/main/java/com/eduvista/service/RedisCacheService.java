package com.eduvista.service;

import com.eduvista.dto.StudentDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final String STUDENT_HASH_PREFIX = "student:hash:";
    private static final Duration STUDENT_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedissonClient redissonClient;

    public void incrementOperationCount(String operation) {
        try {
            String key = "operation:count:" + operation;
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            // Redis 不可用时，静默失败，不影响主流程
        }
    }

    public Long getOperationCount(String operation) {
        try {
            String key = "operation:count:" + operation;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value.toString()) : 0L;
        } catch (Exception e) {
            // Redis 不可用时，返回 0
            return 0L;
        }
    }

    public void cacheStudentHash(StudentDTO student) {
        try {
            if (student == null || student.getId() == null) {
                return;
            }
            String key = STUDENT_HASH_PREFIX + student.getId();
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            hash.putAll(key, Map.of(
                    "id", student.getId(),
                    "studentNo", student.getStudentNo(),
                    "name", student.getName(),
                    "gender", student.getGender() != null ? student.getGender() : "",
                    "phone", student.getPhone() != null ? student.getPhone() : "",
                    "email", student.getEmail() != null ? student.getEmail() : "",
                    "classId", student.getClassId() != null ? student.getClassId() : "",
                    "className", student.getClassName() != null ? student.getClassName() : "",
                    "avatar", student.getAvatar() != null ? student.getAvatar() : ""
            ));
            redisTemplate.expire(key, STUDENT_TTL);
        } catch (Exception e) {
            // Redis 不可用时，静默失败，不影响主流程
        }
    }

    public StudentDTO getStudentFromHash(Long studentId) {
        try {
            if (studentId == null) {
                return null;
            }
            String key = STUDENT_HASH_PREFIX + studentId;
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return null;
            }
            HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
            StudentDTO student = new StudentDTO();
            student.setId(studentId);
            student.setStudentNo(stringVal(hash.get(key, "studentNo")));
            student.setName(stringVal(hash.get(key, "name")));
            student.setGender(stringVal(hash.get(key, "gender")));
            student.setPhone(stringVal(hash.get(key, "phone")));
            student.setEmail(stringVal(hash.get(key, "email")));
            student.setClassName(stringVal(hash.get(key, "className")));
            student.setAvatar(stringVal(hash.get(key, "avatar")));
            Object classIdVal = hash.get(key, "classId");
            if (classIdVal != null && !"".equals(classIdVal.toString())) {
                student.setClassId(Long.parseLong(classIdVal.toString()));
            }
            return student;
        } catch (Exception e) {
            // Redis 不可用时，返回 null，让调用方从数据库查询
            return null;
        }
    }

    private String stringVal(Object value) {
        return value == null ? "" : value.toString();
    }

    public StudentDTO getStudentWithLock(Long studentId, StudentService studentService) {
        // 如果 RedissonClient 不可用，直接查询数据库
        if (redissonClient == null) {
            return studentService.findById(studentId);
        }
        
        String lockKey = "lock:student:" + studentId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    StudentDTO student = getStudentFromHash(studentId);
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
        } catch (Exception e) {
            // Redisson 连接失败时，降级到直接查询
            // 日志已在 Redisson 内部记录
        }
        return studentService.findById(studentId);
    }
}

