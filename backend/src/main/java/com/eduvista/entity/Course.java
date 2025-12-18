package com.eduvista.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String courseNo;
    
    @Column(nullable = false)
    private String courseName;
    
    private Integer credits;
    
    private Integer hours;
    
    private String description;
    
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private Set<Student> students = new HashSet<>();
}

