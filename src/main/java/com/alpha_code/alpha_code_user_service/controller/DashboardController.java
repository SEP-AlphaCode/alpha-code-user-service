package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/online-users")
    @Operation(summary = "Get count of online users")
    public ResponseEntity<Long> countOnlineUsers() {
        return ResponseEntity.ok(service.countOnlineUsers());
    }

    @GetMapping("/stats/{roleName}")
    @Operation(summary = "Get user stats by role (Teacher, Admin, etc.)")
    public ResponseEntity<Map<String, Object>> getStatsByRole(@PathVariable String roleName) {
        Map<String, Object> result = new HashMap<>();
        result.put("role", roleName);
        result.put("total", service.countUsersByRole(roleName));
        result.put("newThisMonth", service.countNewUsersByRoleThisMonth(roleName));
        result.put("growthRate", service.calculateGrowthRateByRole(roleName));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-stats")
    @Operation(summary = "Get user stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        return ResponseEntity.ok(service.getUserStats());
    }

    @GetMapping("/summary")
    @Operation(summary = "Get summary stats")
    public ResponseEntity<Map<String, Long>> getSummaryStats() {
        return ResponseEntity.ok(service.getSummaryStats());
    }

   
}
