package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AccountRepository accountRepository;
//    private final ActivityRepository activityRepository;
//    private final RobotRepository robotRepository;
//    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
//    private final QRCodeRepository qrCodeRepository;
//    private final OsmoCardRepository osmoCardRepository;
//    private final MarkerRepository markerRepository;
//    private final ActivityMapper activityMapper;
    private static final String ONLINE_KEY_PREFIX = "online:user:";

    @Override
    public long countOnlineUsers() {
        Set<String> keys = redisTemplate.keys(ONLINE_KEY_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    @Override
    public void addOnlineUser(UUID accountId) {
        String key = ONLINE_KEY_PREFIX + accountId;
        redisTemplate.opsForValue().set(key, "online", 5, TimeUnit.MINUTES);
    }

    @Override
    public void removeOnlineUser(UUID accountId) {
        String key = ONLINE_KEY_PREFIX + accountId;
        redisTemplate.delete(key);
    }

    @Override
    public long countUsersByRole(String roleName) {
        UUID roleId = getRoleIdByName(roleName);
        return accountRepository.countByRoleId(roleId);
    }

    @Override
    public long countNewUsersByRoleThisMonth(String roleName) {
        UUID roleId = getRoleIdByName(roleName);
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        return accountRepository.countByRoleIdAndCreatedDateBetween(roleId, startOfMonth, endOfMonth);
    }

    @Override
    public double calculateGrowthRateByRole(String roleName) {
        UUID roleId = getRoleIdByName(roleName);
        LocalDate now = LocalDate.now();

        // tháng này
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        long newThisMonth = accountRepository.countByRoleIdAndCreatedDateBetween(roleId, startOfThisMonth, endOfThisMonth);

        // tháng trước
        LocalDateTime startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = startOfThisMonth;
        long newLastMonth = accountRepository.countByRoleIdAndCreatedDateBetween(roleId, startOfLastMonth, endOfLastMonth);

        if (newLastMonth == 0) {
            return newThisMonth > 0 ? 100.0 : 0.0;
        }

        return ((double) (newThisMonth - newLastMonth) / newLastMonth) * 100.0;
    }

    @Override
    public Map<String, Object> getUserStats() {
        LocalDate now = LocalDate.now();

        // tháng này
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        // tháng trước
        LocalDateTime startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = startOfMonth;

        long newUsersThisMonth = accountRepository.countByCreatedDateBetween(startOfMonth, endOfMonth);
        long newUsersLastMonth = accountRepository.countByCreatedDateBetween(startOfLastMonth, endOfLastMonth);
        double newThisMonth;

        if (newUsersLastMonth == 0) {
            newThisMonth = newUsersLastMonth > 0 ? 100.0 : 0.0;
        } else {
            newThisMonth = ((double) (newUsersThisMonth - newUsersLastMonth) / newUsersLastMonth) * 100.0;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAccounts", accountRepository.count());
        stats.put("newUsersThisMonth", newUsersThisMonth);
        stats.put("newUsersLastMonth", newUsersLastMonth);
        stats.put("growthRate", newThisMonth);

        return stats;

    }

    @Override
    @Cacheable(value = "dashboardSummary", key = "'summary'")
    public Map<String, Long> getSummaryStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAccounts", accountRepository.count());
//        stats.put("totalActivities", activityRepository.count());
//        stats.put("totalRobots", robotRepository.count());
//        stats.put("totalOrganizations", organizationRepository.count());
        return stats;
    }

    @Override
    @Cacheable(value = "dashboardExtension", key = "'extension'")
    public Map<String, Long> getExtensionStats() {
        Map<String, Long> stats = new HashMap<>();
//        stats.put("totalQRCodes", qrCodeRepository.count());
//        stats.put("totalOsmoCards", osmoCardRepository.count());
//        stats.put("totalMarkers", markerRepository.count());
//        stats.put("totalActivities", activityRepository.count());

        return stats;
    }

    private UUID getRoleIdByName(String roleName) {
        return roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName))
                .getId();
    }

//    private List<ActivityDto> getTopActivities(String key, int topN) {
//        Set<ZSetOperations.TypedTuple<String>> results =
//                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);
//
//        if (results == null) return List.of();
//
//        return results.stream()
//                .map(tuple -> {
//                    UUID id = UUID.fromString(tuple.getValue());
//                    return activityRepository.findById(id).map(activityMapper::toDto).orElse(null);
//                })
//                .filter(Objects::nonNull)
//                .toList();
//    }

//    @Override
//    public List<ActivityDto> getTopActivitiesToday(int topN) {
//        String keyDay = "activities:day:" + LocalDate.now();
//        return getTopActivities(keyDay, topN);
//    }

//    @Override
//    public List<ActivityDto> getTopActivitiesThisWeek(int topN) {
//        WeekFields weekFields = WeekFields.of(Locale.getDefault());
//        int weekNumber = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
//        String keyWeek = "activities:week:" + LocalDate.now().getYear() + "-" + weekNumber;
//        return getTopActivities(keyWeek, topN);
//    }
//
//    @Override
//    public List<ActivityDto> getTopActivitiesThisMonth(int topN) {
//        String keyMonth = "activities:month:" + YearMonth.now();
//        return getTopActivities(keyMonth, topN);
//    }
}