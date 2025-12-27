package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private UUID accountId;
    private UUID roleId;
    private Role role;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName("USER");
    }

    @Test
    void testCountOnlineUsers() {
        Set<String> keys = Set.of("online:user:1", "online:user:2");
        when(redisTemplate.keys("online:user:*")).thenReturn(keys);

        long result = dashboardService.countOnlineUsers();

        assertEquals(2, result);
        verify(redisTemplate).keys("online:user:*");
    }

    @Test
    void testCountOnlineUsers_NoUsers() {
        when(redisTemplate.keys("online:user:*")).thenReturn(null);

        long result = dashboardService.countOnlineUsers();

        assertEquals(0, result);
    }

    @Test
    void testAddOnlineUser() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        dashboardService.addOnlineUser(accountId);

        verify(redisTemplate.opsForValue()).set(eq("online:user:" + accountId), eq("online"), eq(5L), any());
    }

    @Test
    void testRemoveOnlineUser() {
        dashboardService.removeOnlineUser(accountId);

        verify(redisTemplate).delete("online:user:" + accountId);
    }

    @Test
    void testCountUsersByRole() {
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(java.util.Optional.of(role));
        when(accountRepository.countByRoleId(roleId)).thenReturn(10L);

        long result = dashboardService.countUsersByRole("USER");

        assertEquals(10L, result);
        verify(roleRepository).findByNameIgnoreCase("USER");
        verify(accountRepository).countByRoleId(roleId);
    }

    @Test
    void testCountNewUsersByRoleThisMonth() {
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(java.util.Optional.of(role));
        when(accountRepository.countByRoleIdAndCreatedDateBetween(any(), any(), any())).thenReturn(5L);

        long result = dashboardService.countNewUsersByRoleThisMonth("USER");

        assertEquals(5L, result);
        verify(accountRepository).countByRoleIdAndCreatedDateBetween(any(), any(), any());
    }

    @Test
    void testCalculateGrowthRateByRole() {
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(java.util.Optional.of(role));
        when(accountRepository.countByRoleIdAndCreatedDateBetween(any(), any(), any()))
            .thenReturn(10L)  // this month
            .thenReturn(5L);   // last month

        double result = dashboardService.calculateGrowthRateByRole("USER");

        assertEquals(100.0, result, 0.01);
        verify(accountRepository, times(2)).countByRoleIdAndCreatedDateBetween(any(), any(), any());
    }

    @Test
    void testCalculateGrowthRateByRole_ZeroLastMonth() {
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(java.util.Optional.of(role));
        when(accountRepository.countByRoleIdAndCreatedDateBetween(any(), any(), any()))
            .thenReturn(5L)   // this month
            .thenReturn(0L);  // last month

        double result = dashboardService.calculateGrowthRateByRole("USER");

        assertEquals(100.0, result, 0.01);
    }

    @Test
    void testGetUserStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth;

        when(accountRepository.countByCreatedDateBetween(startOfMonth, endOfMonth)).thenReturn(20L);
        when(accountRepository.countByCreatedDateBetween(startOfLastMonth, endOfLastMonth)).thenReturn(10L);
        when(accountRepository.count()).thenReturn(100L);

        var result = dashboardService.getUserStats();

        assertNotNull(result);
        assertEquals(100L, result.get("totalAccounts"));
        assertEquals(20L, result.get("newUsersThisMonth"));
        assertEquals(10L, result.get("newUsersLastMonth"));
        assertNotNull(result.get("growthRate"));
    }

    @Test
    void testGetSummaryStats() {
        when(accountRepository.count()).thenReturn(100L);

        var result = dashboardService.getSummaryStats();

        assertNotNull(result);
        assertEquals(100L, result.get("totalAccounts"));
        verify(accountRepository).count();
    }

    @Test
    void testGetExtensionStats() {
        var result = dashboardService.getExtensionStats();

        assertNotNull(result);
    }
}

