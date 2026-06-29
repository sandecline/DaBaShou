package com.dabashou.stat.service.impl;

import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatServiceImpl implements StatService {

    private static final Logger log = LoggerFactory.getLogger(StatServiceImpl.class);
    private final JdbcTemplate jdbc;

    public StatServiceImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public OverviewVo getOverview(Long userId) {
        OverviewVo vo = new OverviewVo();
        vo.setTotalOrders(queryInt("SELECT COUNT(*) FROM dbs_order WHERE buyer_id=? OR seller_id=?", userId, userId));
        vo.setCompletedOrders(queryInt("SELECT COUNT(*) FROM dbs_order WHERE (buyer_id=? OR seller_id=?) AND status=5", userId, userId));
        vo.setTotalIncome(queryInt("SELECT IFNULL(SUM(point_amount),0) FROM dbs_order WHERE seller_id=? AND status=5", userId));
        vo.setTotalExpense(queryInt("SELECT IFNULL(SUM(point_amount),0) FROM dbs_order WHERE buyer_id=? AND status=5", userId));

        Map<String, Object> user = queryMap("SELECT trust_score FROM dbs_user WHERE id=?", userId);
        vo.setTrustScore(toBigDecimal(user != null ? user.get("trust_score") : null));

        vo.setSkillCount(queryInt("SELECT COUNT(*) FROM dbs_user_skill WHERE user_id=?", userId));
        vo.setReviewCount(queryInt("SELECT COUNT(*) FROM dbs_review WHERE reviewee_id=?", userId));
        return vo;
    }

    @Override
    public List<TrendItemVo> getOrdersTrend(Long userId, int days) {
        String sql = "SELECT DATE(create_time) as dt, COUNT(*) as cnt FROM dbs_order " +
                "WHERE (buyer_id=? OR seller_id=?) AND create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY dt ORDER BY dt";
        Map<String, Integer> data = new LinkedHashMap<>();
        jdbc.queryForList(sql, userId, userId, days).forEach(r -> data.put(
                r.get("dt").toString(), ((Number) r.get("cnt")).intValue()));

        List<TrendItemVo> list = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            TrendItemVo item = new TrendItemVo();
            String date = start.plusDays(i).format(fmt);
            item.setDate(date);
            item.setValue(data.getOrDefault(date, 0));
            list.add(item);
        }
        return list;
    }

    @Override
    public List<TrendItemVo> getPointsTrend(Long userId, int days) {
        String sql = "SELECT DATE(create_time) as dt, IFNULL(SUM(amount),0) as cnt FROM dbs_point_transaction " +
                "WHERE user_id=? AND create_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY dt ORDER BY dt";
        Map<String, Integer> data = new LinkedHashMap<>();
        jdbc.queryForList(sql, userId, days).forEach(r -> data.put(
                r.get("dt").toString(), ((Number) r.get("cnt")).intValue()));

        List<TrendItemVo> list = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            TrendItemVo item = new TrendItemVo();
            String date = start.plusDays(i).format(fmt);
            item.setDate(date);
            item.setValue(data.getOrDefault(date, 0));
            list.add(item);
        }
        return list;
    }

    @Override
    public List<SkillHeatVo> getSkillsHeat(int limit) {
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

    @Override
    public List<CategoryStatVo> getCategories() {
        String sql = """
                SELECT c.name AS category_name, COUNT(s.id) AS cnt
                FROM dbs_skill_category c
                LEFT JOIN dbs_skill_tag t ON t.category_id = c.id AND t.status = 1
                LEFT JOIN dbs_skill_shelf s ON s.skill_tag_id = t.id
                WHERE c.status = 1
                GROUP BY c.id, c.name""";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        List<CategoryStatVo> list = new ArrayList<>();
        int total = 0;
        for (Map<String, Object> r : rows) {
            CategoryStatVo vo = new CategoryStatVo();
            vo.setCategoryName((String) r.get("category_name"));
            int cnt = ((Number) r.get("cnt")).intValue();
            vo.setCount(cnt);
            list.add(vo);
            total += cnt;
        }
        final int t = Math.max(total, 1);
        list.forEach(v -> v.setPercentage(BigDecimal.valueOf(v.getCount() * 100.0 / t)
                .setScale(1, RoundingMode.HALF_UP)));
        return list;
    }

    private Integer queryInt(String sql, Object... args) {
        Number n = jdbc.queryForObject(sql, Number.class, args);
        return n != null ? n.intValue() : 0;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        return new BigDecimal(v.toString());
    }

    private Map<String, Object> queryMap(String sql, Object... args) {
        try { return jdbc.queryForMap(sql, args); }
        catch (Exception e) { return null; }
    }
}
