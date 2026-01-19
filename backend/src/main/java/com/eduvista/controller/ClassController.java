package com.eduvista.controller;

import com.eduvista.entity.Class;
import com.eduvista.repository.ClassRepository;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassRepository classRepository;

    @GetMapping("/list")
    public CommonResponse<List<Class>> getClassList() {
        List<Class> classes = classRepository.findAll();
        return CommonResponse.success(classes);
    }
}