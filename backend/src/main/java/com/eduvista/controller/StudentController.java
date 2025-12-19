package com.eduvista.controller;

import com.eduvista.entity.Student;
import com.eduvista.service.StudentService;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<Page<Student>> getStudentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Student> students;
        if (keyword != null && !keyword.isEmpty()) {
            students = studentService.search(keyword, pageable);
        } else {
            students = studentService.findAll(pageable);
        }
        return CommonResponse.success(students);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return CommonResponse.success(student);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Student> createStudent(@RequestBody Student student) {
        Student saved = studentService.save(student);
        return CommonResponse.success(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id);
        Student updated = studentService.save(student);
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

        Student student = studentService.findById(id);
        student.setAvatar(avatarUrl);
        studentService.save(student);

        Map<String, String> result = new HashMap<>();
        result.put("avatar", avatarUrl);

        return CommonResponse.success(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public CommonResponse<List<Student>> getAllStudents() {
        List<Student> students = studentService.findAll();
        return CommonResponse.success(students);
    }
}

