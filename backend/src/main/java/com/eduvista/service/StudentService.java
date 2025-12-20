package com.eduvista.service;

import com.eduvista.util.PageResponse;
import com.eduvista.dto.StudentDTO;
import com.eduvista.entity.Class;
import com.eduvista.entity.Student;
import com.eduvista.kafka.KafkaProducer;
import com.eduvista.repository.ClassRepository;
import com.eduvista.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "students") // 统一缓存命名空间
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    
    @Autowired(required = false)
    private KafkaProducer kafkaProducer;

    /**
     * 将 Entity 转换为 DTO 的私有方法
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        // BeanUtils 可以自动拷贝名称相同的属性
        BeanUtils.copyProperties(student, dto);

        // 处理关联属性（避免懒加载异常，因为此时 Session 是开启的）
        if (student.getStudentClass() != null) {
            dto.setClassId(student.getStudentClass().getId());
            dto.setClassName(student.getStudentClass().getClassName());
        }
        return dto;
    }

    /**
     * 将 DTO 转换为 Entity 的私有方法
     */
    private Student convertToEntity(StudentDTO dto) {
        Student student = new Student();
        BeanUtils.copyProperties(dto, student, "id", "classId", "className", "createdAt", "updatedAt");

        // 处理班级关联
        if (dto.getClassId() != null) {
            Class studentClass = classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在"));
            student.setStudentClass(studentClass);
        }

        return student;
    }

    @Cacheable(key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<StudentDTO> findAll(Pageable pageable) {
        // 1. 从数据库查出 Page<Student>
        Page<Student> page = studentRepository.findAll(pageable);

        // 2. 转换数据列表
        List<StudentDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 3. 封装并返回（存入 Redis 的将是 PageResponse<StudentDTO>）
        return new PageResponse<>(
                dtoList,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }

    /**
     * 获取所有学生（不分页）
     */
    public List<StudentDTO> findAll() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(key = "'id:' + #id")
    public StudentDTO findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        return convertToDTO(student);
    }

    /**
     * 搜索学生
     */
    @Cacheable(key = "'search:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<StudentDTO> search(String keyword, Pageable pageable) {
        Page<Student> page = studentRepository.searchByKeyword(keyword, pageable);
        
        List<StudentDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                dtoList,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }

    /**
     * 保存学生（接收 DTO，返回 DTO）
     */
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO save(StudentDTO dto) {
        Student student;
        
        if (dto.getId() != null) {
            // 更新：先查询现有实体
            student = studentRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("学生不存在"));
            // 更新属性
            BeanUtils.copyProperties(dto, student, "id", "classId", "className", "createdAt");
            
            // 更新班级关联
            if (dto.getClassId() != null) {
                Class studentClass = classRepository.findById(dto.getClassId())
                        .orElseThrow(() -> new RuntimeException("班级不存在"));
                student.setStudentClass(studentClass);
            }
        } else {
            // 新增：转换为实体
            student = convertToEntity(dto);
        }
        
        Student saved = studentRepository.save(student);
        StudentDTO savedDTO = convertToDTO(saved);
        
        // 如果是新增学生，尝试发送 Kafka 消息，并记录发送状态
        if (dto.getId() == null) {
            boolean kafkaSuccess = false;
            if (kafkaProducer != null) {
                try {
                    kafkaProducer.sendStudentRegisterMessage(
                        savedDTO.getStudentNo(),
                        savedDTO.getName(),
                        savedDTO.getEmail()
                    );
                    kafkaProducer.sendWelcomeMessage(
                        savedDTO.getStudentNo(),
                        savedDTO.getName()
                    );
                    kafkaSuccess = true;
                } catch (Exception e) {
                    // Kafka 发送失败不影响主流程，只记录日志
                    // 日志已在 KafkaProducer 中记录
                    kafkaSuccess = false;
                }
            } else {
                // Kafka Producer 不可用（可能 Kafka 未配置或未启用）
                kafkaSuccess = false;
            }
            // 始终设置 Kafka 消息状态，即使 Kafka 不可用也要返回状态
            savedDTO.setKafkaMessageSent(kafkaSuccess);
        }
        
        return savedDTO;
    }

    /**
     * 根据ID删除学生
     */
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public void deleteById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("学生不存在");
        }
        studentRepository.deleteById(id);
    }
}