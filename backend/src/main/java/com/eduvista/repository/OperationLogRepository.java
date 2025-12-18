package com.eduvista.repository;

import com.eduvista.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    @Query("SELECT o.operation, COUNT(o) as count FROM OperationLog o WHERE o.createTime >= :startTime GROUP BY o.operation")
    List<Object[]> countOperationsByTypeSince(@org.springframework.data.repository.query.Param("startTime") LocalDateTime startTime);
    
    Page<OperationLog> findByUsername(String username, Pageable pageable);
    
    @Query("SELECT DATE(o.createTime) as date, COUNT(o) as count FROM OperationLog o WHERE o.createTime >= :startTime GROUP BY DATE(o.createTime) ORDER BY date")
    List<Object[]> countOperationsByDate(@org.springframework.data.repository.query.Param("startTime") LocalDateTime startTime);
}

