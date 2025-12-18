package com.eduvista.controller;

import com.eduvista.repository.OperationLogRepository;
import com.eduvista.service.RedisCacheService;
import com.eduvista.util.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final RedisCacheService redisCacheService;
    private final OperationLogRepository operationLogRepository;
    
    @GetMapping("/operation-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> getOperationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Map<String, Long> operationCounts = new HashMap<>();
        operationCounts.put("getStudentById", redisCacheService.getOperationCount("getStudentById"));
        operationCounts.put("getStudentList", redisCacheService.getOperationCount("getStudentList"));
        operationCounts.put("createStudent", redisCacheService.getOperationCount("createStudent"));
        operationCounts.put("updateStudent", redisCacheService.getOperationCount("updateStudent"));
        operationCounts.put("deleteStudent", redisCacheService.getOperationCount("deleteStudent"));
        operationCounts.put("login", redisCacheService.getOperationCount("login"));
        
        stats.put("operationCounts", operationCounts);
        
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> dailyCounts = operationLogRepository.countOperationsByDate(weekAgo);
        
        Map<String, Long> dailyStats = new HashMap<>();
        for (Object[] row : dailyCounts) {
            dailyStats.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        
        stats.put("dailyStats", dailyStats);
        
        return CommonResponse.success(stats);
    }
}

