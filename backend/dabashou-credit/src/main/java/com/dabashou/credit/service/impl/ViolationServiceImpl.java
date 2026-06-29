package com.dabashou.credit.service.impl;

import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.mapper.ViolationMapper;
import com.dabashou.credit.service.ViolationService;
import com.dabashou.credit.vo.ViolationVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 违规举报服务实现
 */
@Service
public class ViolationServiceImpl implements ViolationService {

    private final ViolationMapper violationMapper;
    private final JdbcTemplate jdbcTemplate;

    public ViolationServiceImpl(ViolationMapper violationMapper, JdbcTemplate jdbcTemplate) {
        this.violationMapper = violationMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long report(Long userId, ViolationDto dto) {
        // 证据处理
        String evidenceStr = null;
        if (dto.getEvidence() != null && dto.getEvidence().length > 0) {
            evidenceStr = String.join(",", dto.getEvidence());
        }

        // 构建 description，包含原因和证据
        String description = dto.getReason();
        if (evidenceStr != null && !evidenceStr.isEmpty()) {
            description = description + "\n[证据]:" + evidenceStr;
        }

        // 插入违规记录，reporter_id = 当前用户，status = 1（有效）
        String insertSql = "INSERT INTO credit_violation (user_id, order_id, type, description, penalty_score, reporter_id, status, create_time, update_time) VALUES (?, ?, ?, ?, 0, ?, 1, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, dto.getTargetUserId());
            if (dto.getOrderId() != null) {
                ps.setLong(2, dto.getOrderId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, dto.getType());
            ps.setString(4, description);
            ps.setLong(5, userId);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(6, now);
            ps.setTimestamp(7, now);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "举报提交失败");
        }
        return key.longValue();
    }

    @Override
    public PageResult<ViolationVo> listMine(Long userId, int pageNum, int pageSize) {
        // 查询总数（我被举报的记录）
        String countSql = "SELECT COUNT(1) FROM credit_violation WHERE user_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, userId);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 分页查询
        String listSql = "SELECT * FROM credit_violation WHERE user_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, userId, pageSize, offset);

        // 收集举报人ID
        List<Long> reporterIds = rows.stream()
                .map(r -> ((Number) r.get("reporter_id")).longValue())
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户昵称
        Map<Long, String> nicknameMap = batchQueryNicknames(reporterIds);

        // 映射为VO
        List<ViolationVo> vos = rows.stream().map(r -> {
            ViolationVo vo = new ViolationVo();
            vo.setId(((Number) r.get("id")).longValue());
            vo.setUserId(((Number) r.get("user_id")).longValue());
            Object orderIdObj = r.get("order_id");
            if (orderIdObj != null) {
                vo.setOrderId(((Number) orderIdObj).longValue());
            }
            vo.setType((String) r.get("type"));
            vo.setDescription((String) r.get("description"));
            Object penaltyObj = r.get("penalty_score");
            if (penaltyObj != null) {
                vo.setPenaltyScore(new java.math.BigDecimal(penaltyObj.toString()));
            }
            Long reporterId = ((Number) r.get("reporter_id")).longValue();
            vo.setReporterId(reporterId);
            vo.setReporterNickname(nicknameMap.get(reporterId));
            vo.setStatus(((Number) r.get("status")).intValue());

            Object createTime = r.get("create_time");
            if (createTime instanceof LocalDateTime) {
                vo.setCreateTime((LocalDateTime) createTime);
            } else if (createTime instanceof Timestamp) {
                vo.setCreateTime(((Timestamp) createTime).toLocalDateTime());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(total, vos, pageNum, pageSize);
    }

    /**
     * 批量查询用户昵称
     */
    private Map<Long, String> batchQueryNicknames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, nickname FROM dbs_user WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userIds.toArray());
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> (String) r.get("nickname")
        ));
    }
}
