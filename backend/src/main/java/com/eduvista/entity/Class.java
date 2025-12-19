package com.eduvista.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String classNo;

    @Column(nullable = false)
    private String className;

    private String grade;

    private String major;

    private String description;

    @OneToMany(mappedBy = "studentClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();

//     可选：如果您希望在序列化时避免循环引用，可以添加以下方法
     @JsonManagedReference
     public List<Student> getStudents() {
         return students;
     }
}