package com.eduvista.config;

import com.eduvista.entity.Class;
import com.eduvista.entity.Role;
import com.eduvista.entity.Student;
import com.eduvista.entity.User;
import com.eduvista.repository.ClassRepository;
import com.eduvista.repository.RoleRepository;
import com.eduvista.repository.StudentRepository;
import com.eduvista.repository.UserRepository;
import com.eduvista.service.RedisCacheService;
import com.eduvista.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final RedisCacheService redisCacheService;
    
    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdmin();
        initializeClasses();
        initializeStudents();
        warmupCache();
        log.info("数据初始化完成");
    }
    
    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder()
                .name("ADMIN")
                .description("管理员")
                .build();
            
            Role studentRole = Role.builder()
                .name("STUDENT")
                .description("学生")
                .build();
            
            roleRepository.save(adminRole);
            roleRepository.save(studentRole);
            log.info("角色初始化完成");
        }
    }
    
    private void initializeAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@eduvista.com")
                .nickname("管理员")
                .roles(Set.of(adminRole))
                .build();
            
            userRepository.save(admin);
            log.info("管理员账户初始化完成: admin/admin123");
        }
    }
    
    private void initializeClasses() {
        if (classRepository.count() == 0) {
            Class class1 = Class.builder()
                .classNo("CS2024-01")
                .className("计算机科学2024级1班")
                .grade("2024")
                .major("计算机科学与技术")
                .description("计算机科学专业2024级1班")
                .build();
            
            Class class2 = Class.builder()
                .classNo("SE2024-01")
                .className("软件工程2024级1班")
                .grade("2024")
                .major("软件工程")
                .description("软件工程专业2024级1班")
                .build();
            
            classRepository.save(class1);
            classRepository.save(class2);
            log.info("班级初始化完成");
        }
    }
    
    private void initializeStudents() {
        if (studentRepository.count() == 0) {
            Class class1 = classRepository.findByClassNo("CS2024-01").orElseThrow();
            
            Student student1 = Student.builder()
                .studentNo("2024001")
                .name("张三")
                .gender("男")
                .birthDate(LocalDate.of(2002, 5, 15))
                .phone("13800138001")
                .email("zhangsan@example.com")
                .address("北京市朝阳区")
                .studentClass(class1)
                .build();
            
            Student student2 = Student.builder()
                .studentNo("2024002")
                .name("李四")
                .gender("女")
                .birthDate(LocalDate.of(2002, 8, 20))
                .phone("13800138002")
                .email("lisi@example.com")
                .address("上海市浦东新区")
                .studentClass(class1)
                .build();
            
            studentRepository.save(student1);
            studentRepository.save(student2);
            log.info("学生数据初始化完成");
        }
    }
    
    private void warmupCache() {
        try {
            log.info("开始预热Redis缓存...");
            studentService.findAll().forEach(student -> {
                try {
                    redisCacheService.cacheStudentHash(student);
                } catch (Exception e) {
                    log.warn("缓存学生数据失败: {}", e.getMessage());
                }
            });
            log.info("Redis缓存预热完成");
        } catch (Exception e) {
            log.warn("Redis缓存预热失败，应用将继续运行: {}", e.getMessage());
        }
    }
}

