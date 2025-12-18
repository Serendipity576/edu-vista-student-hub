package com.eduvista.service;

import com.eduvista.dto.PageResponse;
import com.eduvista.entity.Student;
import com.eduvista.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Cacheable(value = "students", key = "#id")
    public Student findById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("学生不存在"));
    }

    // --- 修改点 1: findAll ---
    @Cacheable(value = "students", key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<Student> findAll(Pageable pageable) {
        Page<Student> page = studentRepository.findAll(pageable);
        return PageResponse.of(page); // 转换为自定义对象
    }

    // --- 修改点 2: search ---
    @Cacheable(value = "students", key = "'search:' + #keyword + ':p:' + #pageable.pageNumber")
    public PageResponse<Student> search(String keyword, Pageable pageable) {
        Page<Student> page = studentRepository.searchByKeyword(keyword, pageable);
        return PageResponse.of(page); // 转换为自定义对象
    }

    @CacheEvict(value = "students", allEntries = true)
    @Transactional
    public Student save(Student student) {
        if (student.getId() == null && studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new RuntimeException("学号已存在");
        }
        return studentRepository.save(student);
    }

    @CacheEvict(value = "students", allEntries = true)
    @Transactional
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }
}