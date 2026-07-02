package com.dabashou.stat.service.impl;

import com.dabashou.stat.service.AdminStatService;
import com.dabashou.stat.vo.*;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStatServiceImpl implements AdminStatService {

    private final JdbcTemplate jdbc;

    public AdminStatServiceImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public AdminOverviewVo getOverview() {
        AdminOverviewVo vo = new AdminOverviewVo();
        int totalOrders = qi("SELECT COUNT(*) FROM dbs_order");
        int completedOrders = qi("SELECT COUNT(*) FROM dbs_order WHERE status=5");
        vo.setTotalUsers(qi("SELECT COUNT(*) FROM dbs_user"));
        vo.setTotalOrders(totalOrders);
        vo.setCompletedOrders(completedOrders);
        vo.setTotalShelves(qi("SELECT COUNT(*) FROM dbs_skill_shelf WHERE status=1"));
        vo.setTotalSkills(qi("SELECT COUNT(*) FROM dbs_skill_shelf"));
        vo.setTotalDemands(qi("SELECT COUNT(*) FROM dbs_demand WHERE status=1"));
        vo.setTodayNewUsers(qi("SELECT COUNT(*) FROM dbs_user WHERE DATE(create_time)=CURDATE()"));
        vo.setTodayNewOrders(qi("SELECT COUNT(*) FROM dbs_order WHERE DATE(create_time)=CURDATE()"));
        vo.setOrderCompletionRate(totalOrders > 0
                ? BigDecimal.valueOf(completedOrders * 1.0 / totalOrders).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setTotalPointsInCirculation(qi("SELECT IFNULL(SUM(point_balance),0) FROM dbs_user"));
        vo.setPendingAppeals(qi("SELECT COUNT(*) FROM credit_appeal WHERE status=0"));
        vo.setDisputingOrders(qi("SELECT COUNT(*) FROM dbs_order WHERE status=7"));
        vo.setPendingCampusAuths(hasTable("dbs_user_campus_auth") ? qi("SELECT COUNT(*) FROM dbs_user_campus_auth WHERE status=0") : 0);
        vo.setPendingViolations(qi("SELECT COUNT(*) FROM credit_violation WHERE status=0"));
        return vo;
    }

    @Override
    public List<DailyTrendVo> getDailyTrend(int days) {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_user WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "newUserCount"));
        jdbc.queryForList("""
                SELECT DATE(create_time) dt, COUNT(DISTINCT CASE WHEN buyer_id IS NOT NULL THEN buyer_id END) +
                COUNT(DISTINCT CASE WHEN seller_id IS NOT NULL AND seller_id != buyer_id THEN seller_id END) cnt
                FROM dbs_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt
                """, days)
                .forEach(r -> data(map, r, "activeUserCount"));
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "newOrderCount"));
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_order WHERE status=5 AND create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "completedOrderCount"));

        Map<String, Integer> inflowMap = new LinkedHashMap<>();
        Map<String, Integer> outflowMap = new LinkedHashMap<>();
        try {
            jdbc.queryForList("""
                    SELECT DATE(create_time) dt,
                           SUM(CASE WHEN type IN (1, 5) THEN amount ELSE 0 END) AS inflow,
                           SUM(CASE WHEN type IN (2, 3) THEN amount ELSE 0 END) AS outflow
                    FROM dbs_point_transaction
                    WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY)
                    GROUP BY dt
                    """, days).forEach(r -> {
                String dt = r.get("dt").toString();
                inflowMap.put(dt, ((Number) r.get("inflow")).intValue());
                outflowMap.put(dt, ((Number) r.get("outflow")).intValue());
            });
        } catch (Exception ignored) {
        }

        List<DailyTrendVo> list = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            String date = start.plusDays(i).format(fmt);
            Map<String, Integer> m = map.getOrDefault(date, Map.of());
            DailyTrendVo vo = new DailyTrendVo();
            vo.setDate(date);
            vo.setNewUserCount(m.getOrDefault("newUserCount", 0));
            vo.setActiveUserCount(m.getOrDefault("activeUserCount", 0));
            vo.setNewOrderCount(m.getOrDefault("newOrderCount", 0));
            vo.setCompletedOrderCount(m.getOrDefault("completedOrderCount", 0));
            vo.setPointInflow(inflowMap.getOrDefault(date, 0));
            vo.setPointOutflow(outflowMap.getOrDefault(date, 0));
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<TrendItemVo> getUserActive(int days) {
        String sql = "SELECT DATE(create_time) dt, COUNT(DISTINCT buyer_id) cnt FROM dbs_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt ORDER BY dt";
        Map<String, Integer> data = new LinkedHashMap<>();
        jdbc.queryForList(sql, days).forEach(r -> data.put(r.get("dt").toString(), ((Number) r.get("cnt")).intValue()));
        List<TrendItemVo> list = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            String date = start.plusDays(i).format(fmt);
            TrendItemVo item = new TrendItemVo();
            item.setDate(date);
            item.setValue(data.getOrDefault(date, 0));
            list.add(item);
        }
        return list;
    }

    @Override
    public List<TrustDistributionVo> getTrustDistribution() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT
                  CASE WHEN trust_score < 3.0 THEN '新人'
                       WHEN trust_score < 4.0 THEN '靠谱'
                       ELSE '金牌' END AS level,
                  COUNT(*) AS cnt
                FROM dbs_user
                GROUP BY level
                """);
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("新人", 0);
        map.put("靠谱", 0);
        map.put("金牌", 0);
        for (Map<String, Object> r : rows) {
            String level = r.get("level").toString();
            int cnt = ((Number) r.get("cnt")).intValue();
            map.put(level, cnt);
        }
        int total = map.values().stream().mapToInt(i -> i).sum();
        return map.entrySet().stream().map(e -> {
            TrustDistributionVo vo = new TrustDistributionVo();
            vo.setLevel(e.getKey());
            vo.setCount(e.getValue());
            vo.setPercentage(total > 0 ? BigDecimal.valueOf(e.getValue() * 100.0 / total).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public byte[] exportData(String type) {
        return "stub export".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<SkillHeatVo> getSkillHeat(int limit) {
        String sql = """
                SELECT t.id, t.name,
                    (SELECT COUNT(*) FROM dbs_skill_shelf s WHERE s.skill_tag_id=t.id) AS shelf_count,
                    (SELECT COUNT(*) FROM dbs_demand d WHERE d.skill_tag_id=t.id) AS demand_count,
                    (SELECT COUNT(*) FROM dbs_order o WHERE o.skill_tag_id=t.id) AS order_count
                FROM dbs_skill_tag t ORDER BY order_count DESC LIMIT ?""";
        return jdbc.queryForList(sql, limit).stream().map(r -> {
            SkillHeatVo vo = new SkillHeatVo();
            vo.setSkillTagId(((Number) r.get("id")).longValue());
            vo.setSkillTagName((String) r.get("name"));
            int shelf = ((Number) r.get("shelf_count")).intValue();
            int demand = ((Number) r.get("demand_count")).intValue();
            int order = ((Number) r.get("order_count")).intValue();
            vo.setShelfCount(shelf);
            vo.setDemandCount(demand);
            vo.setOrderCount(order);
            vo.setHeatScore(BigDecimal.valueOf(shelf * 1L + demand * 2L + order * 3L));
            return vo;
        }).collect(Collectors.toList());
    }

    private void data(Map<String, Map<String, Integer>> map, Map<String, Object> r, String key) {
        String dt = r.get("dt").toString();
        map.computeIfAbsent(dt, k -> new HashMap<>()).put(key, ((Number) r.get("cnt")).intValue());
    }

    private Integer qi(String sql, Object... args) {
        Number n = jdbc.queryForObject(sql, Number.class, args);
        return n != null ? n.intValue() : 0;
    }

    private boolean hasTable(String tableName) {
        try {
            return Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
                String upperName = tableName.toUpperCase(Locale.ROOT);
                try (ResultSet rs = connection.getMetaData().getTables(null, null, upperName, new String[]{"TABLE"})) {
                    if (rs.next()) return true;
                }
                try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
                    return rs.next();
                }
            }));
        } catch (Exception e) {
            return false;
        }
    }
}
