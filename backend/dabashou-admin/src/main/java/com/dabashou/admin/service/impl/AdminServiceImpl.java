package com.dabashou.admin.service.impl;

import com.dabashou.admin.dto.AdminDto;
import com.dabashou.admin.service.AdminService;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.order.dto.ArbitrateDto;
import com.dabashou.order.service.OrderService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Set<String> CONFIG_WHITELIST = Set.of(
            "site.name", "site.notice", "order.auto_cancel_minutes",
            "order.confirm_timeout_hours", "order.verify_code_minutes",
            "order.buyer_cancel_penalty", "order.seller_cancel_penalty",
            "point.sign_in_reward", "point.register_bonus",
            "credit.violation_penalty", "credit.newcomer_max", "credit.reliable_max"
    );
    private static final Set<String> SENSITIVE_CONFIG_KEYS = Set.of("jwt.secret", "sms.secret", "payment.secret");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    public AdminServiceImpl(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, OrderService orderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
    }

    @Override
    public PageResult<Map<String, Object>> listUsers(String keyword, Integer status, int pageNum, int pageSize) {
        List<Object> args = new ArrayList<>();
        String where = userWhere(keyword, status, args);
        String campusAuthSelect = campusAuthStatusSelect();
        long total = count("SELECT COUNT(*) FROM dbs_user u " + where, args);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT u.id, u.username, u.nickname, u.avatar, u.phone, u.point_balance AS pointBalance,
                       u.trust_score AS trustScore, u.campus, u.status, u.create_time AS createTime,
                       NULL AS lastLoginTime,
                       (SELECT GROUP_CONCAT(r.role_code)
                          FROM sys_user_role ur
                          JOIN sys_role r ON r.id = ur.role_id
                         WHERE ur.user_id = u.id) AS roles,
                       """ + campusAuthSelect + """
                  FROM dbs_user u
                """ + where + " ORDER BY u.create_time DESC LIMIT ? OFFSET ?", withPageArgs(args, pageNum, pageSize));
        rows.forEach(this::normalizeUserRow);
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getUserDetail(Long id) {
        String campusAuthSelect = campusAuthStatusSelect();
        Map<String, Object> row = queryOne("""
                SELECT u.id, u.username, u.nickname, u.avatar, u.phone, u.email, u.point_balance AS pointBalance,
                       u.trust_score AS trustScore, u.campus, u.building, u.bio, u.status,
                       u.create_time AS createTime, NULL AS lastLoginTime,
                       (SELECT GROUP_CONCAT(r.role_code)
                          FROM sys_user_role ur
                          JOIN sys_role r ON r.id = ur.role_id
                         WHERE ur.user_id = u.id) AS roles,
                       """ + campusAuthSelect + """
                  FROM dbs_user u WHERE u.id = ?
                """, id);
        normalizeUserRow(row);
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long adminId, Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户状态只能为0或1");
        }
        int updated = updateUserStatusWithTokenInvalidation(userId, status);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + userId);
        }
        audit(adminId, "ADMIN_USER_STATUS", "更新用户状态 userId=" + userId + ", status=" + status, 1, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resetPassword(Long adminId, Long userId) {
        String newPassword = temporaryPassword();
        int updated = resetPasswordWithTokenInvalidation(userId, passwordEncoder.encode(newPassword));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + userId);
        }
        audit(adminId, "ADMIN_RESET_PASSWORD", "重置用户密码 userId=" + userId, 1, null);
        return Map.of("newPassword", newPassword);
    }

    @Override
    public PageResult<Map<String, Object>> listOrders(String keyword, Integer status, int pageNum, int pageSize) {
        List<Object> args = new ArrayList<>();
        String where = orderWhere(keyword, status, args);
        long total = count("SELECT COUNT(*) FROM dbs_order o LEFT JOIN dbs_user b ON b.id = o.buyer_id LEFT JOIN dbs_user s ON s.id = o.seller_id " + where, args);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT o.id, o.order_no AS orderNo, o.buyer_id AS buyerId, b.nickname AS buyerNickname,
                       o.seller_id AS sellerId, s.nickname AS sellerNickname, o.title, o.point_amount AS pointAmount,
                       o.status, o.create_time AS createTime
                  FROM dbs_order o
                  LEFT JOIN dbs_user b ON b.id = o.buyer_id
                  LEFT JOIN dbs_user s ON s.id = o.seller_id
                """ + where + " ORDER BY o.create_time DESC LIMIT ? OFFSET ?", withPageArgs(args, pageNum, pageSize));
        rows.forEach(this::addOrderStatusDesc);
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id) {
        Map<String, Object> row = queryOne("""
                SELECT o.id, o.order_no AS orderNo, o.buyer_id AS buyerId, b.nickname AS buyerNickname,
                       o.seller_id AS sellerId, s.nickname AS sellerNickname, o.skill_shelf_id AS shelfId,
                       sh.title AS shelfTitle, o.demand_id AS demandId, d.title AS demandTitle,
                       o.title, o.point_amount AS pointAmount, o.status, o.remark,
                       o.cancel_reason AS cancelReason, o.create_time AS createTime
                  FROM dbs_order o
                  LEFT JOIN dbs_user b ON b.id = o.buyer_id
                  LEFT JOIN dbs_user s ON s.id = o.seller_id
                  LEFT JOIN dbs_skill_shelf sh ON sh.id = o.skill_shelf_id
                  LEFT JOIN dbs_demand d ON d.id = o.demand_id
                 WHERE o.id = ?
                """, id);
        addOrderStatusDesc(row);
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arbitrateOrder(Long adminId, Long id, AdminDto.ArbitrateRequest request) {
        ArbitrateDto dto = new ArbitrateDto();
        dto.setResult(request.getResult() + (request.getReason() == null ? "" : ": " + request.getReason()));
        dto.setRefundAmount(request.getRefundAmount());
        orderService.arbitrateOrder(id, dto);
        audit(adminId, "ADMIN_ORDER_ARBITRATE", "订单仲裁 orderId=" + id + ", result=" + request.getResult(), 1, null);
    }

    @Override
    public PageResult<Map<String, Object>> listViolations(int pageNum, int pageSize) {
        long total = count("SELECT COUNT(*) FROM credit_violation", List.of());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT v.id, v.user_id AS targetUserId, u.nickname AS targetNickname, v.reporter_id AS reporterId,
                       r.nickname AS reporterNickname, v.order_id AS orderId, v.type, v.description AS reason,
                       v.description, v.status, v.create_time AS createTime
                  FROM credit_violation v
                  LEFT JOIN dbs_user u ON u.id = v.user_id
                  LEFT JOIN dbs_user r ON r.id = v.reporter_id
                 ORDER BY v.create_time DESC LIMIT ? OFFSET ?
                """, pageSize, offset(pageNum, pageSize));
        rows.forEach(this::normalizeViolationRow);
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleViolation(Long adminId, Long id, AdminDto.ViolationHandleRequest request) {
        Map<String, Object> violation = queryOne("SELECT id, user_id, order_id, penalty_score FROM credit_violation WHERE id = ?", id);
        jdbcTemplate.update("UPDATE credit_violation SET status = 1, description = ?, update_time = ? WHERE id = ?",
                request.getResult(), LocalDateTime.now(), id);
        addTrustLog(asLong(violation.get("user_id")), asLong(violation.get("order_id")), "violation", -0.5, request.getResult());
        audit(adminId, "ADMIN_VIOLATION_HANDLE", "处理违规 violationId=" + id, 1, null);
    }

    @Override
    public PageResult<Map<String, Object>> listAppeals(int pageNum, int pageSize) {
        long total = count("SELECT COUNT(*) FROM credit_appeal", List.of());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT a.id, a.violation_id AS violationId, a.appellant_id AS appellantId, u.nickname AS appellantNickname,
                       a.reason, a.status, a.review_remark AS reviewRemark, a.create_time AS createTime
                  FROM credit_appeal a
                  LEFT JOIN dbs_user u ON u.id = a.appellant_id
                 ORDER BY a.create_time DESC LIMIT ? OFFSET ?
                """, pageSize, offset(pageNum, pageSize));
        rows.forEach(this::normalizeAppealRow);
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppeal(Long adminId, Long id, AdminDto.AppealHandleRequest request) {
        int status = Boolean.TRUE.equals(request.getApproved()) ? 1 : 2;
        int updated = jdbcTemplate.update("""
                UPDATE credit_appeal
                   SET status = ?, reviewer_id = ?, review_remark = ?, review_time = ?, update_time = ?
                 WHERE id = ?
                """, status, adminId, request.getReason(), LocalDateTime.now(), LocalDateTime.now(), id);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在: " + id);
        }
        audit(adminId, "ADMIN_APPEAL_HANDLE", "处理申诉 appealId=" + id + ", approved=" + request.getApproved(), 1, null);
    }

    @Override
    public PageResult<Map<String, Object>> listReviews(int pageNum, int pageSize) {
        long total = count("SELECT COUNT(*) FROM dbs_review WHERE COALESCE(hidden, 0) = 0", List.of());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT rv.id, rv.order_id AS orderId, o.title AS orderTitle, rv.rating, rv.content,
                       rv.reviewer_id AS reviewerId, r.nickname AS reviewerName,
                       rv.reviewee_id AS revieweeId, e.nickname AS revieweeName, rv.create_time AS createTime
                  FROM dbs_review rv
                  LEFT JOIN dbs_order o ON o.id = rv.order_id
                  LEFT JOIN dbs_user r ON r.id = rv.reviewer_id
                  LEFT JOIN dbs_user e ON e.id = rv.reviewee_id
                 WHERE COALESCE(rv.hidden, 0) = 0
                 ORDER BY rv.create_time DESC LIMIT ? OFFSET ?
                """, pageSize, offset(pageNum, pageSize));
        rows.forEach(row -> row.put("images", List.of()));
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hideReview(Long adminId, Long id) {
        int updated = jdbcTemplate.update("UPDATE dbs_review SET hidden = 1 WHERE id = ?", id);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在: " + id);
        }
        audit(adminId, "ADMIN_REVIEW_HIDE", "隐藏评价 reviewId=" + id, 1, null);
    }

    @Override
    public PageResult<Map<String, Object>> listCampusAuths(Integer status, int pageNum, int pageSize) {
        if (!hasTable("dbs_user_campus_auth")) {
            return PageResult.of(0, List.of(), pageNum, pageSize);
        }
        List<Object> args = new ArrayList<>();
        String where = "";
        if (status != null) {
            where = " WHERE a.status = ? ";
            args.add(status);
        }
        long total = count("SELECT COUNT(*) FROM dbs_user_campus_auth a" + where, args);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT a.id, a.user_id AS userId, u.nickname, a.auth_type AS authType, a.student_no AS studentNo,
                       a.real_name AS realName, a.campus, a.college, a.status, a.review_remark AS reviewRemark,
                       a.create_time AS createTime
                  FROM dbs_user_campus_auth a
                  LEFT JOIN dbs_user u ON u.id = a.user_id
                """ + where + " ORDER BY a.create_time DESC LIMIT ? OFFSET ?", withPageArgs(args, pageNum, pageSize));
        rows.forEach(row -> row.put("statusDesc", authStatusDesc(asInt(row.get("status")))));
        return PageResult.of(total, rows, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewCampusAuth(Long adminId, Long id, AdminDto.CampusAuthReviewRequest request) {
        if (!hasTable("dbs_user_campus_auth")) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "校园认证功能未初始化");
        }
        int status = Boolean.TRUE.equals(request.getApproved()) ? 1 : 2;
        Map<String, Object> auth = queryOne("SELECT user_id, campus FROM dbs_user_campus_auth WHERE id = ?", id);
        int updated = jdbcTemplate.update("""
                UPDATE dbs_user_campus_auth
                   SET status = ?, reviewer_id = ?, review_remark = ?, review_time = ?, update_time = ?
                 WHERE id = ?
                """, status, adminId, request.getReason(), LocalDateTime.now(), LocalDateTime.now(), id);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "校园认证不存在: " + id);
        }
        if (status == 1) {
            jdbcTemplate.update("UPDATE dbs_user SET campus = COALESCE(?, campus), update_time = ? WHERE id = ?",
                    auth.get("campus"), LocalDateTime.now(), auth.get("user_id"));
        }
        audit(adminId, "ADMIN_CAMPUS_AUTH_REVIEW", "校园认证审核 authId=" + id + ", status=" + status, 1, null);
    }

    @Override
    public Map<String, Object> getConfig() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT config_key, config_value FROM sys_config WHERE config_key IN (" + placeholders(CONFIG_WHITELIST.size()) + ")",
                CONFIG_WHITELIST.toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = asString(row.get("config_key"));
            result.put(key, SENSITIVE_CONFIG_KEYS.contains(key) ? "******" : row.get("config_value"));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long adminId, Map<String, Object> config) {
        Map<String, Object> payload = unwrapConfig(config);
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            if (!CONFIG_WHITELIST.contains(key) || SENSITIVE_CONFIG_KEYS.contains(key)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "非法配置项: " + key);
            }
            Long exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_config WHERE config_key = ?", Long.class, key);
            if (exists != null && exists > 0) {
                jdbcTemplate.update("UPDATE sys_config SET config_value = ?, update_time = ? WHERE config_key = ?",
                        String.valueOf(entry.getValue()), LocalDateTime.now(), key);
            } else {
                jdbcTemplate.update("""
                        INSERT INTO sys_config (config_key, config_value, config_name, config_type, description)
                        VALUES (?, ?, ?, 'business', '后台配置')
                        """, key, String.valueOf(entry.getValue()), key);
            }
        }
        audit(adminId, "ADMIN_CONFIG_UPDATE", "更新系统配置 keys=" + payload.keySet(), 1, null);
    }

    private String userWhere(String keyword, Integer status, List<Object> args) {
        List<String> parts = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            parts.add("(u.username LIKE ? OR u.nickname LIKE ? OR u.phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (status != null) {
            parts.add("u.status = ?");
            args.add(status);
        }
        return parts.isEmpty() ? "" : " WHERE " + String.join(" AND ", parts);
    }

    private String orderWhere(String keyword, Integer status, List<Object> args) {
        List<String> parts = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            parts.add("(o.order_no LIKE ? OR o.title LIKE ? OR b.nickname LIKE ? OR s.nickname LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (status != null) {
            parts.add("o.status = ?");
            args.add(status);
        }
        return parts.isEmpty() ? "" : " WHERE " + String.join(" AND ", parts);
    }

    private void normalizeUserRow(Map<String, Object> row) {
        row.put("phone", maskPhone(asString(row.get("phone"))));
    }

    private String campusAuthStatusSelect() {
        if (!hasTable("dbs_user_campus_auth")) {
            return "NULL AS campusAuthStatus";
        }
        return """
                (SELECT a.status
                   FROM dbs_user_campus_auth a
                  WHERE a.user_id = u.id
                  ORDER BY a.create_time DESC LIMIT 1) AS campusAuthStatus
                """;
    }

    private void normalizeViolationRow(Map<String, Object> row) {
        row.put("typeDesc", asString(row.get("type")));
        row.put("statusDesc", asInt(row.get("status")) == 0 ? "已撤销" : "有效");
        row.put("handleResult", row.get("description"));
        row.put("evidence", List.of());
    }

    private void normalizeAppealRow(Map<String, Object> row) {
        row.put("statusDesc", authStatusDesc(asInt(row.get("status"))));
        row.put("evidence", List.of());
    }

    private void addOrderStatusDesc(Map<String, Object> row) {
        row.put("statusDesc", switch (asInt(row.get("status"))) {
            case 0 -> "已取消";
            case 1 -> "待支付";
            case 2 -> "已支付（担保中）";
            case 3 -> "服务中";
            case 4 -> "待确认";
            case 5 -> "已完成";
            case 6 -> "已退款";
            case 7 -> "争议中";
            default -> "未知";
        });
    }

    private void addTrustLog(Long userId, Long orderId, String type, double scoreChange, String reason) {
        if (userId == null) {
            return;
        }
        Map<String, Object> user = queryOne("SELECT trust_score FROM dbs_user WHERE id = ?", userId);
        double before = asDouble(user.get("trust_score"), 5.0);
        double after = Math.max(0, Math.min(5, before + scoreChange));
        jdbcTemplate.update("UPDATE dbs_user SET trust_score = ?, update_time = ? WHERE id = ?", after, LocalDateTime.now(), userId);
        jdbcTemplate.update("""
                INSERT INTO dbs_user_trust_score_log (user_id, order_id, type, score_change, score_before, score_after, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, userId, orderId, type, scoreChange, before, after, reason);
    }

    private void audit(Long operatorId, String type, String content, int status, String errorMsg) {
        jdbcTemplate.update("""
                INSERT INTO sys_log (operator_id, operation_type, operation_content, method, request_url, request_params, status, error_msg, cost_time_ms)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, operatorId, type, content, "ADMIN", "/api/admin/v1", content, status, errorMsg, 0);
    }

    private Map<String, Object> queryOne(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForMap(sql, args);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
    }

    private long count(String sql, List<Object> args) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    private Object[] withPageArgs(List<Object> args, int pageNum, int pageSize) {
        List<Object> all = new ArrayList<>(args);
        all.add(pageSize);
        all.add(offset(pageNum, pageSize));
        return all.toArray();
    }

    private int offset(int pageNum, int pageSize) {
        return Math.max(0, pageNum - 1) * pageSize;
    }

    private String temporaryPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder builder = new StringBuilder("Dbs");
        for (int i = 0; i < 9; i++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String authStatusDesc(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "待审核";
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapConfig(Map<String, Object> config) {
        Object nested = config.get("config");
        if (nested instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return config;
    }

    private int updateUserStatusWithTokenInvalidation(Long userId, Integer status) {
        try {
            return jdbcTemplate.update(
                    "UPDATE dbs_user SET status = ?, token_version = COALESCE(token_version, 0) + 1, update_time = ? WHERE id = ?",
                    status, LocalDateTime.now(), userId);
        } catch (DataAccessException e) {
            return jdbcTemplate.update("UPDATE dbs_user SET status = ?, update_time = ? WHERE id = ?",
                    status, LocalDateTime.now(), userId);
        }
    }

    private int resetPasswordWithTokenInvalidation(Long userId, String passwordHash) {
        try {
            return jdbcTemplate.update(
                    "UPDATE dbs_user SET password_hash = ?, token_version = COALESCE(token_version, 0) + 1, update_time = ? WHERE id = ?",
                    passwordHash, LocalDateTime.now(), userId);
        } catch (DataAccessException e) {
            return jdbcTemplate.update("UPDATE dbs_user SET password_hash = ?, update_time = ? WHERE id = ?",
                    passwordHash, LocalDateTime.now(), userId);
        }
    }

    private String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    private boolean hasTable(String tableName) {
        try {
            return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
                String upperName = tableName.toUpperCase(Locale.ROOT);
                try (ResultSet rs = connection.getMetaData().getTables(null, null, upperName, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
                try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
                    return rs.next();
                }
            }));
        } catch (DataAccessException e) {
            return false;
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }

    private Integer asInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(value.toString());
    }

    private double asDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(value.toString());
    }
}
