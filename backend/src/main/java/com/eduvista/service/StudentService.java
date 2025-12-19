package com.eduvista.service;

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

    @Cacheable(value = "students", key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Cacheable(value = "students", key = "'search:' + #keyword")
    public Page<Student> search(String keyword, Pageable pageable) {
        return studentRepository.searchByKeyword(keyword, pageable);
    }

    @CacheEvict(value = "students", allEntries = true)
    @Transactional
    public Student save(Student student) {
        if (student.getId() == null && studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new RuntimeException("学号已存在");
        }
        Student saved = studentRepository.save(student);
        return saved;
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

