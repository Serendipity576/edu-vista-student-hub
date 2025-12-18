package com.eduvista.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    private String operation;
    
    private String method;
    
    private String url;
    
    private String ip;
    
    @Column(length = 1000)
    private String params;
    
    private Long executionTime;
    
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

