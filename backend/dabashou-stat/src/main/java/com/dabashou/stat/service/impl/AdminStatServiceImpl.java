package com.dabashou.stat.service.impl;

import com.dabashou.stat.service.AdminStatService;
import com.dabashou.stat.vo.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
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
        vo.setTotalUsers(qi("SELECT COUNT(*) FROM dbs_user"));
        vo.setTotalOrders(qi("SELECT COUNT(*) FROM dbs_order"));
        vo.setTotalShelves(qi("SELECT COUNT(*) FROM dbs_skill_shelf WHERE status=1"));
        vo.setTotalDemands(qi("SELECT COUNT(*) FROM dbs_demand WHERE status=1"));
        vo.setTodayNewUsers(qi("SELECT COUNT(*) FROM dbs_user WHERE DATE(create_time)=CURDATE()"));
        vo.setTodayNewOrders(qi("SELECT COUNT(*) FROM dbs_order WHERE DATE(create_time)=CURDATE()"));
        return vo;
    }

    @Override
    public List<DailyTrendVo> getDailyTrend(int days) {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_user WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "newUserCount"));
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(DISTINCT buyer_id) + COUNT(DISTINCT seller_id) cnt FROM dbs_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "activeUserCount"));
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "newOrderCount"));
        jdbc.queryForList("SELECT DATE(create_time) dt, COUNT(*) cnt FROM dbs_order WHERE status=5 AND create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt", days)
                .forEach(r -> data(map, r, "completedOrderCount"));

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
            vo.setPointInflow(0);
            vo.setPointOutflow(0);
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
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("新人", 0);
        map.put("靠谱", 0);
        map.put("金牌", 0);
        jdbc.queryForList("SELECT trust_score FROM dbs_user").forEach(r -> {
            Object v = r.get("trust_score");
            if (v == null) return;
            double s = v instanceof BigDecimal ? ((BigDecimal) v).doubleValue() : Double.parseDouble(v.toString());
            if (s < 3.0) map.merge("新人", 1, Integer::sum);
            else if (s < 4.0) map.merge("靠谱", 1, Integer::sum);
            else map.merge("金牌", 1, Integer::sum);
        });
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

    private void data(Map<String, Map<String, Integer>> map, Map<String, Object> r, String key) {
        String dt = r.get("dt").toString();
        map.computeIfAbsent(dt, k -> new HashMap<>()).put(key, ((Number) r.get("cnt")).intValue());
    }

    private Integer qi(String sql, Object... args) {
        Number n = jdbc.queryForObject(sql, Number.class, args);
        return n != null ? n.intValue() : 0;
    }
}
