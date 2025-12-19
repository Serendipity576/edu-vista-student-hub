package com.eduvista.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String address;
    private String avatar;

    // 只存储班级名称或ID，不存储复杂的 Class 实体对象，彻底避免循环引用
    private String className;
    private Long classId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}