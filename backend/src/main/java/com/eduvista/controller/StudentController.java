package com.eduvista.controller;

import com.eduvista.util.PageResponse; // 必须导入你刚才创建的 PageResponse 类
import com.eduvista.dto.StudentDTO;
import com.eduvista.service.StudentService;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 修改点：将返回类型改为 CommonResponse<PageResponse<StudentDTO>>
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<PageResponse<StudentDTO>> getStudentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // 修改点：变量类型也改为 PageResponse<StudentDTO>
        PageResponse<StudentDTO> students;

        if (keyword != null && !keyword.isEmpty()) {
            students = studentService.search(keyword, pageable);
        } else {
            students = studentService.findAll(pageable);
        }

        return CommonResponse.success(students);
    }

    // --- 以下方法保持不变 ---

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.findById(id);
        return CommonResponse.success(student);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> createStudent(@RequestBody StudentDTO student) {
        StudentDTO saved = studentService.save(student);
        
        // 构建响应，包含学生信息和 Kafka 消息状态
        Map<String, Object> response = new HashMap<>();
        response.put("student", saved);
        
        // 如果是新增学生（id 为 null），总是返回 Kafka 消息状态
        if (student.getId() == null) {
            Boolean kafkaSent = saved.getKafkaMessageSent();
            if (kafkaSent == null) {
                // 如果 Kafka Producer 不可用，设置为 false
                kafkaSent = false;
            }
            response.put("kafkaMessageSent", kafkaSent);
            if (kafkaSent) {
                response.put("kafkaMessage", "Kafka 消息已成功发送：学生注册消息和欢迎消息已发送到消息队列");
            } else {
                response.put("kafkaMessage", "Kafka 消息发送失败：请检查 Kafka 服务是否正常运行");
            }
        }
        
        return CommonResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<StudentDTO> updateStudent(@PathVariable Long id, @RequestBody StudentDTO student) {
        student.setId(id);
        StudentDTO updated = studentService.save(student);
        return CommonResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return CommonResponse.success(null);
    }

    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<Map<String, String>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return CommonResponse.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String filename = UUID.randomUUID().toString() + extension;

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        String avatarUrl = "/api/uploads/" + filename;

        StudentDTO student = studentService.findById(id);
        student.setAvatar(avatarUrl);
        studentService.save(student);

        Map<String, String> result = new HashMap<>();
        result.put("avatar", avatarUrl);

        return CommonResponse.success(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.findAll();
        return CommonResponse.success(students);
    }
}