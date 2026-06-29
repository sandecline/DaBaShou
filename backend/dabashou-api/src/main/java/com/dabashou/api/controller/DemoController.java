package com.dabashou.api.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 快速演示控制器 — 绕过存根模块，直接用JdbcTemplate查询数据库
 * 仅用于前端测试，后续替换为正式模块实现
 */
@RestController
public class DemoController {

    private final JdbcTemplate jdbc;
    private final String jwtSecret;
    private final long jwtExpiration;

    public DemoController(JdbcTemplate jdbc,
                          @Value("${dabashou.jwt.secret}") String jwtSecret,
                          @Value("${dabashou.jwt.expiration}") long jwtExpiration) {
        this.jdbc = jdbc;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    // ==================== 认证 ====================
    // 前端调用 /api/user/login

    @PostMapping("/api/user/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 演示模式：任意密码登录（生产环境需验证BCrypt哈希）
        List<Map<String, Object>> users = jdbc.queryForList(
            "SELECT id, username, nickname, avatar, phone, " +
            "point_balance AS pointBalance, trust_score AS trustScore, campus, building, bio " +
            "FROM dbs_user WHERE username = ?",
            username);
        if (users.isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", 400);
            err.put("msg", "用户不存在，测试账号：zhangsan / lisi / wangwu");
            err.put("data", null);
            return err;
        }

        Map<String, Object> user = users.get(0);
        Long userId = ((Number) user.get("id")).longValue();
        String token = JwtUtil.generateToken(userId, List.of("USER"), jwtSecret, jwtExpiration);

        return Map.of("code", 200, "msg", "success", "data", Map.of("token", token, "userInfo", user));
    }

    @PostMapping("/api/user/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        jdbc.update(
            "INSERT INTO dbs_user (username, nickname, password_hash, phone, point_balance, trust_score) VALUES (?, ?, ?, ?, 100, 5.0)",
            body.get("username"), body.getOrDefault("nickname", body.get("username")),
            "$2a$10$dummy_hash", body.getOrDefault("phone", ""));
        return Map.of("msg", "注册成功");
    }

    // ==================== 技能分类 ====================

    @GetMapping("/api/skill/categories")
    public List<Map<String, Object>> categories() {
        return jdbc.queryForList("SELECT id, name, icon, sort_order FROM dbs_skill_category WHERE status = 1 ORDER BY sort_order");
    }

    @GetMapping("/api/skill/tags")
    public List<Map<String, Object>> tags(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null) {
            return jdbc.queryForList("SELECT id, category_id, name FROM dbs_skill_tag WHERE status = 1 AND category_id = ?", categoryId);
        }
        return jdbc.queryForList("SELECT id, category_id, name FROM dbs_skill_tag WHERE status = 1");
    }

    // ==================== 技能货架 ====================

    @GetMapping("/api/shelf")
    public Map<String, Object> shelfList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "heat") String sort) {

        StringBuilder sql = new StringBuilder(
            "SELECT s.id, s.user_id AS userId, s.skill_tag_id AS skillTagId, s.title, s.description, " +
            "s.point_price AS pointPrice, s.duration_minutes AS durationMinutes, " +
            "s.location_type AS locationType, s.status, s.create_time AS createTime, " +
            "u.nickname AS userName, u.avatar AS userAvatar, u.trust_score AS trustScore, u.campus, " +
            "t.name AS tagName, c.name AS categoryName, " +
            "CONCAT('https://picsum.photos/seed/', " +
            "CASE c.name " +
            "WHEN '学业辅导' THEN 'study' WHEN '维修帮忙' THEN 'repair' " +
            "WHEN '设计美工' THEN 'design' WHEN '技术支持' THEN 'coding' " +
            "WHEN '运动陪练' THEN 'sports' WHEN '音乐艺术' THEN 'music' " +
            "WHEN '生活服务' THEN 'service' ELSE 'help' END, " +
            "'-', s.id, '/400/300') AS coverImage " +
            "FROM dbs_skill_shelf s " +
            "JOIN dbs_user u ON s.user_id = u.id " +
            "JOIN dbs_skill_tag t ON s.skill_tag_id = t.id " +
            "JOIN dbs_skill_category c ON t.category_id = c.id " +
            "WHERE s.status = 1");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.title LIKE ? OR s.description LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND c.id = ?");
            params.add(categoryId);
        }

        String orderBy = switch (sort) {
            case "distance" -> "u.campus";
            case "trust" -> "u.trust_score DESC";
            case "latest" -> "s.create_time DESC";
            default -> "s.id DESC"; // heat
        };
        sql.append(" ORDER BY ").append(orderBy);

        // 分页
        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> records = jdbc.queryForList(sql.toString(), params.toArray());

        // 总数
        String countSql = "SELECT COUNT(*) FROM dbs_skill_shelf s JOIN dbs_skill_tag t ON s.skill_tag_id = t.id JOIN dbs_skill_category c ON t.category_id = c.id WHERE s.status = 1";
        int total = jdbc.queryForObject(countSql, Integer.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    // ==================== 技能详情 ====================

    @GetMapping("/api/shelf/{id}")
    public Map<String, Object> shelfDetail(@PathVariable int id) {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT s.id, s.user_id AS userId, s.skill_tag_id AS skillTagId, s.title, s.description, " +
            "s.point_price AS pointPrice, s.duration_minutes AS durationMinutes, " +
            "s.location_type AS locationType, s.status, s.create_time AS createTime, " +
            "u.nickname AS userName, u.avatar AS userAvatar, u.trust_score AS trustScore, " +
            "u.campus, u.building, u.bio, t.name AS tagName, c.name AS categoryName, " +
            "CONCAT('https://picsum.photos/seed/', " +
            "CASE c.name " +
            "WHEN '学业辅导' THEN 'study' WHEN '维修帮忙' THEN 'repair' " +
            "WHEN '设计美工' THEN 'design' WHEN '技术支持' THEN 'coding' " +
            "WHEN '运动陪练' THEN 'sports' WHEN '音乐艺术' THEN 'music' " +
            "WHEN '生活服务' THEN 'service' ELSE 'help' END, " +
            "'-', s.id, '/400/300') AS coverImage " +
            "FROM dbs_skill_shelf s " +
            "JOIN dbs_user u ON s.user_id = u.id " +
            "JOIN dbs_skill_tag t ON s.skill_tag_id = t.id " +
            "JOIN dbs_skill_category c ON t.category_id = c.id " +
            "WHERE s.id = ?", id);
        if (list.isEmpty()) throw new RuntimeException("技能不存在");
        return list.get(0);
    }

    // ==================== 需求 ====================

    @GetMapping("/api/demand")
    public Map<String, Object> demandList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "latest") String sort) {

        StringBuilder sql = new StringBuilder(
            "SELECT d.id, d.user_id AS userId, d.skill_tag_id AS skillTagId, d.title, d.description, " +
            "d.point_reward AS pointReward, d.deadline, d.location_type AS locationType, " +
            "d.campus, d.building, d.status, d.create_time AS createTime, " +
            "u.nickname AS userName, u.avatar AS userAvatar, u.trust_score, " +
            "t.name AS tagName " +
            "FROM dbs_demand d " +
            "JOIN dbs_user u ON d.user_id = u.id " +
            "LEFT JOIN dbs_skill_tag t ON d.skill_tag_id = t.id " +
            "WHERE 1=1");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (d.title LIKE ? OR d.description LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        if (status != null && status > 0) {
            sql.append(" AND d.status = ?");
            params.add(status);
        }

        String orderBy = "latest".equals(sort) ? "d.create_time DESC" : "d.point_reward DESC";
        sql.append(" ORDER BY ").append(orderBy);

        int offset = (page - 1) * size;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> records = jdbc.queryForList(sql.toString(), params.toArray());

        int total = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_demand", Integer.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @GetMapping("/api/demand/{id}")
    public Map<String, Object> demandDetail(@PathVariable int id) {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT d.id, d.user_id AS userId, d.skill_tag_id AS skillTagId, d.title, d.description, " +
            "d.point_reward AS pointReward, d.deadline, d.location_type AS locationType, " +
            "d.campus, d.building, d.status, d.create_time AS createTime, " +
            "u.nickname AS userName, u.avatar AS userAvatar, u.trust_score AS trustScore, " +
            "u.campus AS userCampus, u.building AS userBuilding, u.bio, t.name AS tagName " +
            "FROM dbs_demand d " +
            "JOIN dbs_user u ON d.user_id = u.id " +
            "LEFT JOIN dbs_skill_tag t ON d.skill_tag_id = t.id " +
            "WHERE d.id = ?", id);
        if (list.isEmpty()) throw new RuntimeException("需求不存在");
        return list.get(0);
    }

    // ==================== 用户 ====================

    @GetMapping("/api/user/profile")
    public Map<String, Object> profile(@RequestAttribute(required = false) Long userId) {
        if (userId == null) userId = 1L; // demo fallback
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT id, username, nickname, avatar, phone, email, " +
            "point_balance AS pointBalance, trust_score AS trustScore, campus, building, bio " +
            "FROM dbs_user WHERE id = ?", userId);
        if (list.isEmpty()) throw new RuntimeException("用户不存在");
        return list.get(0);
    }

    @GetMapping("/api/stat/overview")
    public Map<String, Object> overview() {
        int totalUsers = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_user", Integer.class);
        int totalSkills = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_skill_shelf WHERE status = 1", Integer.class);
        int completedOrders = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_order WHERE status = 5", Integer.class);
        int totalOrders = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_order", Integer.class);
        double rate = totalOrders > 0 ? (double) completedOrders / totalOrders : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", totalUsers);
        result.put("totalSkills", totalSkills);
        result.put("completedOrders", completedOrders);
        result.put("totalOrders", totalOrders);
        result.put("orderCompletionRate", rate);
        return result;
    }

    // ==================== 前端路径别名 (带 /list) ====================

    @GetMapping("/api/shelf/list")
    public Map<String, Object> shelfListAlias(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "heat") String sort) {
        return shelfList(page, size, keyword, categoryId, sort);
    }

    // ==================== 后端路径别名及写操作 ====================

    @PostMapping("/api/shelf")
    public Map<String, Object> publishShelf(@RequestBody Map<String, Object> body) {
        jdbc.update(
            "INSERT INTO dbs_skill_shelf (user_id, skill_tag_id, title, description, point_price, duration_minutes, location_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 1)",
            1, // 演示：默认当前用户ID=1
            body.get("skillTagId") != null ? ((Number) body.get("skillTagId")).intValue() : 1,
            body.get("title") != null ? body.get("title").toString() : "",
            body.get("description") != null ? body.get("description").toString() : "",
            body.get("pointPrice") != null ? ((Number) body.get("pointPrice")).intValue() : 100,
            body.get("durationMinutes") != null ? ((Number) body.get("durationMinutes")).intValue() : 60,
            body.get("locationType") != null ? ((Number) body.get("locationType")).intValue() : 1);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "发布成功！");
        result.put("data", Map.of("id", jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class)));
        return result;
    }

    @PostMapping("/api/demand")
    public Map<String, Object> publishDemand(@RequestBody Map<String, Object> body) {
        jdbc.update(
            "INSERT INTO dbs_demand (user_id, skill_tag_id, title, description, point_reward, deadline, location_type, campus, building, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)",
            1, // 演示：默认当前用户ID=1
            body.get("skillTagId") != null ? ((Number) body.get("skillTagId")).intValue() : 1,
            body.get("title") != null ? body.get("title").toString() : "",
            body.get("description") != null ? body.get("description").toString() : "",
            body.get("pointReward") != null ? ((Number) body.get("pointReward")).intValue() : 100,
            body.get("deadline") != null ? body.get("deadline").toString() : null,
            body.get("locationType") != null ? ((Number) body.get("locationType")).intValue() : 1,
            body.get("campus") != null ? body.get("campus").toString() : null,
            body.get("building") != null ? body.get("building").toString() : null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "发布成功！");
        result.put("data", Map.of("id", jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class)));
        return result;
    }

    @GetMapping("/api/shelf/my")
    public Map<String, Object> myShelfList(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        // 演示：返回所有上架技能（当作当前用户的）
        return shelfList(page, size, null, null, "latest");
    }

    @PutMapping("/api/shelf/{id}")
    public Map<String, Object> updateShelf(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "更新成功"); result.put("data", null);
        return result;
    }

    @DeleteMapping("/api/shelf/{id}")
    public Map<String, Object> deleteShelf(@PathVariable int id) {
        jdbc.update("UPDATE dbs_skill_shelf SET status = 0 WHERE id = ?", id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "下架成功"); result.put("data", null);
        return result;
    }

    @GetMapping("/api/demand/list")
    public Map<String, Object> demandListAlias(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "latest") String sort) {
        return demandList(page, size, keyword, status, sort);
    }

    // ==================== 订单 ====================

    @PostMapping("/api/order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> body) {
        Integer demandId = body.get("demandId") != null ? ((Number) body.get("demandId")).intValue() : null;
        Integer skillShelfId = body.get("skillShelfId") != null ? ((Number) body.get("skillShelfId")).intValue() : null;
        Integer timeSlotId = body.get("timeSlotId") != null ? ((Number) body.get("timeSlotId")).intValue() : null;

        // 从需求创建订单
        if (demandId != null) {
            List<Map<String, Object>> demands = jdbc.queryForList(
                "SELECT * FROM dbs_demand WHERE id = ? AND status = 1", demandId);
            if (demands.isEmpty()) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("code", 400); err.put("msg", "该需求已不存在或已被接单"); err.put("data", null);
                return err;
            }
            Map<String, Object> demand = demands.get(0);
            int buyerId = ((Number) demand.get("user_id")).intValue();
            int sellerId = 1; // 演示：默认当前登录用户=1(张三)

            // 不能接自己的单
            if (buyerId == sellerId) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("code", 400); err.put("msg", "不能接自己发布的需求哦~"); err.put("data", null);
                return err;
            }

            // 更新需求状态为进行中
            jdbc.update("UPDATE dbs_demand SET status = 2 WHERE id = ?", demandId);

            // 创建订单
            String orderNo = "DB" + System.currentTimeMillis();
            jdbc.update(
                "INSERT INTO dbs_order (order_no, buyer_id, seller_id, demand_id, skill_tag_id, title, point_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                orderNo, buyerId, sellerId, demandId,
                demand.get("skill_tag_id") != null ? ((Number) demand.get("skill_tag_id")).intValue() : 1,
                demand.get("title"), ((Number) demand.get("point_reward")).intValue(), 1); // status=1 待支付

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class));
            result.put("orderNo", orderNo);
            result.put("msg", "接单成功！");
            return result;
        }

        // 从技能货架下单
        if (skillShelfId != null) {
            List<Map<String, Object>> shelves = jdbc.queryForList(
                "SELECT * FROM dbs_skill_shelf WHERE id = ? AND status = 1", skillShelfId);
            if (shelves.isEmpty()) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("code", 400); err.put("msg", "该技能已下架"); err.put("data", null);
                return err;
            }
            Map<String, Object> shelf = shelves.get(0);
            int sellerId = ((Number) shelf.get("user_id")).intValue();
            int buyerId = 1; // 演示：默认当前登录用户=1(张三)

            // 不能买自己的技能
            if (buyerId == sellerId) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("code", 400); err.put("msg", "不能购买自己的技能哦~"); err.put("data", null);
                return err;
            }

            String orderNo = "DB" + System.currentTimeMillis();
            jdbc.update(
                "INSERT INTO dbs_order (order_no, buyer_id, seller_id, skill_shelf_id, skill_tag_id, title, point_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                orderNo, buyerId, sellerId, skillShelfId,
                shelf.get("skill_tag_id"), shelf.get("title"),
                ((Number) shelf.get("point_price")).intValue(), 1);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class));
            result.put("orderNo", orderNo);
            result.put("msg", "下单成功！");
            return result;
        }

        Map<String, Object> err = new LinkedHashMap<>();
        err.put("code", 400); err.put("msg", "请提供 demandId 或 skillShelfId"); err.put("data", null);
        return err;
    }

    @GetMapping("/api/order/list")
    public Map<String, Object> orderList(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) Integer status) {
        List<Map<String, Object>> records = jdbc.queryForList(
            "SELECT o.id, o.order_no AS orderNo, o.buyer_id AS buyerId, o.seller_id AS sellerId, " +
            "o.demand_id AS demandId, o.skill_shelf_id AS skillShelfId, o.skill_tag_id AS skillTagId, " +
            "o.title, o.point_amount AS pointAmount, o.status, o.verify_code AS verifyCode, " +
            "o.create_time AS createTime, o.update_time AS updateTime, " +
            "bu.nickname AS buyerName, su.nickname AS sellerName, t.name AS tagName " +
            "FROM dbs_order o " +
            "JOIN dbs_user bu ON o.buyer_id = bu.id " +
            "JOIN dbs_user su ON o.seller_id = su.id " +
            "JOIN dbs_skill_tag t ON o.skill_tag_id = t.id " +
            "ORDER BY o.create_time DESC LIMIT ? OFFSET ?", size, (page - 1) * size);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM dbs_order", Integer.class);
        return Map.of("records", records, "total", total, "page", page, "size", size);
    }

    @GetMapping("/api/order/my")
    public Map<String, Object> myOrders(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return orderList(page, size, "my", null);
    }

    @GetMapping("/api/order/taken")
    public Map<String, Object> takenOrders(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return orderList(page, size, "taken", null);
    }

    @GetMapping("/api/order/{id}")
    public Map<String, Object> orderDetail(@PathVariable int id) {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT o.id, o.order_no AS orderNo, o.buyer_id AS buyerId, o.seller_id AS sellerId, " +
            "o.demand_id AS demandId, o.skill_shelf_id AS skillShelfId, o.skill_tag_id AS skillTagId, " +
            "o.title, o.point_amount AS pointAmount, o.status, o.verify_code AS verifyCode, " +
            "o.verify_code_expire AS verifyCodeExpire, o.create_time AS createTime, o.update_time AS updateTime, " +
            "bu.nickname AS buyerName, bu.avatar AS buyerAvatar, " +
            "su.nickname AS sellerName, su.avatar AS sellerAvatar, t.name AS tagName " +
            "FROM dbs_order o " +
            "JOIN dbs_user bu ON o.buyer_id = bu.id " +
            "JOIN dbs_user su ON o.seller_id = su.id " +
            "JOIN dbs_skill_tag t ON o.skill_tag_id = t.id " +
            "WHERE o.id = ?", id);
        if (list.isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("code", 404); err.put("msg", "订单不存在"); err.put("data", null);
            return err;
        }
        return list.get(0);
    }

    // ==================== 订单操作 ====================

    @PostMapping("/api/order/{id}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable int id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "用户取消") : "用户取消";
        // 恢复关联需求为可接单状态
        jdbc.update("UPDATE dbs_demand d JOIN dbs_order o ON d.id = o.demand_id SET d.status = 1 WHERE o.id = ? AND o.demand_id IS NOT NULL", id);
        jdbc.update("UPDATE dbs_order SET status = 0, cancel_reason = ?, cancel_time = NOW() WHERE id = ?", reason, id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "订单已取消，需求已恢复"); result.put("data", null);
        return result;
    }

    @PostMapping("/api/order/{id}/pay")
    public Map<String, Object> payOrder(@PathVariable int id) {
        jdbc.update("UPDATE dbs_order SET status = 2 WHERE id = ? AND status = 1", id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "支付成功"); result.put("data", null);
        return result;
    }

    @PostMapping("/api/order/{id}/start")
    public Map<String, Object> startService(@PathVariable int id) {
        jdbc.update("UPDATE dbs_order SET status = 3 WHERE id = ? AND status = 2", id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "服务已开始"); result.put("data", null);
        return result;
    }

    @PostMapping("/api/order/{id}/confirm")
    public Map<String, Object> confirmComplete(@PathVariable int id) {
        jdbc.update("UPDATE dbs_order SET status = 5, complete_time = NOW() WHERE id = ?", id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200); result.put("msg", "已确认完成"); result.put("data", null);
        return result;
    }

    // ==================== 消息 (stub) ====================

    @GetMapping("/api/message/conversations")
    public List<Map<String, Object>> conversations() { return List.of(); }

    @GetMapping("/api/message/unread")
    public Map<String, Object> unread() { return Map.of("count", 0); }

    // ==================== 积分 (stub) ====================

    @GetMapping("/api/point/balance")
    public Map<String, Object> balance() {
        return Map.of("available", 500, "frozen", 0, "balance", 500);
    }

    @GetMapping("/api/point/transactions")
    public Map<String, Object> transactions(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Map.of("records", List.of(), "total", 0, "page", page, "size", size);
    }

    // ==================== 统计 (stub) ====================

    @GetMapping("/api/stat/user")
    public Map<String, Object> userStat() {
        return Map.of("publishedSkills", 2, "publishedDemands", 1, "takenOrders", 0,
                      "completedOrders", 0, "averageRating", 3.5, "totalPointsEarned", 200, "totalPointsSpent", 50);
    }

    @GetMapping("/api/stat/daily")
    public List<Map<String, Object>> dailySummary(@RequestParam(defaultValue = "7") int days) {
        return List.of();
    }

    @GetMapping("/api/stat/skill-heat")
    public List<Map<String, Object>> skillHeat() {
        return List.of();
    }

    @GetMapping("/api/stat/demand")
    public List<Map<String, Object>> demandStat() {
        return List.of();
    }

    @GetMapping("/api/stat/skill")
    public List<Map<String, Object>> skillStat() {
        return List.of();
    }

    // ==================== 信用评价 (stub) ====================

    @GetMapping("/api/credit/reviews")
    public Map<String, Object> creditReviews(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "received") String type) {
        return Map.of("records", List.of(), "total", 0, "page", page, "size", size);
    }

    @GetMapping("/api/credit/violations")
    public Map<String, Object> creditViolations(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Map.of("records", List.of(), "total", 0, "page", page, "size", size);
    }

    @GetMapping("/api/credit/appeals")
    public Map<String, Object> creditAppeals(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Map.of("records", List.of(), "total", 0, "page", page, "size", size);
    }

    // ==================== 用户扩展 ====================

    @GetMapping("/api/user/{id}")
    public Map<String, Object> userById(@PathVariable int id) {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT id, username, nickname, avatar, phone, " +
            "point_balance AS pointBalance, trust_score AS trustScore, campus, building, bio " +
            "FROM dbs_user WHERE id = ?", id);
        if (list.isEmpty()) throw new RuntimeException("用户不存在");
        return list.get(0);
    }

    @GetMapping("/api/user/skills")
    public List<Map<String, Object>> userSkills(@RequestParam(required = false) Integer userId) {
        if (userId == null) return List.of();
        return jdbc.queryForList(
            "SELECT s.id, s.title, s.point_price, t.name AS tagName FROM dbs_skill_shelf s JOIN dbs_skill_tag t ON s.skill_tag_id = t.id WHERE s.user_id = ?", userId);
    }
}
