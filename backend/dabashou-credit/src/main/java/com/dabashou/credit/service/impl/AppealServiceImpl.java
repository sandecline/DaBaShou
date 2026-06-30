package com.dabashou.credit.service.impl;

import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.mapper.AppealMapper;
import com.dabashou.credit.service.AppealService;
import com.dabashou.credit.vo.AppealVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 申诉服务实现
 */
@Service
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;
    private final JdbcTemplate jdbcTemplate;

    public AppealServiceImpl(AppealMapper appealMapper, JdbcTemplate jdbcTemplate) {
        this.appealMapper = appealMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Long userId, AppealDto dto) {
        // 校验违规记录存在
        String violationSql = "SELECT user_id FROM credit_violation WHERE id = ?";
        List<Map<String, Object>> violationRows = jdbcTemplate.queryForList(violationSql, dto.getViolationId());
        if (violationRows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "违规记录不存在");
        }

        Long violationUserId = ((Number) violationRows.get(0).get("user_id")).longValue();

        // 校验当前用户是否为违规记录的当事人
        if (!userId.equals(violationUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能为自己的违规记录申诉");
        }

        // 校验是否已申诉
        String checkSql = "SELECT COUNT(1) FROM credit_appeal WHERE violation_id = ? AND appellant_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, dto.getViolationId(), userId);
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该违规记录已申诉，请勿重复提交");
        }

        // 处理证据
        final String evidenceFileId;
        if (dto.getEvidence() != null && dto.getEvidence().length > 0) {
            evidenceFileId = String.join(",", dto.getEvidence());
        } else {
            evidenceFileId = null;
        }

        // 插入申诉，status = 0（待审核）
        String insertSql = "INSERT INTO credit_appeal (violation_id, appellant_id, reason, evidence_file_id, status, create_time, update_time) VALUES (?, ?, ?, ?, 0, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, dto.getViolationId());
            ps.setLong(2, userId);
            ps.setString(3, dto.getReason());
            ps.setString(4, evidenceFileId);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "申诉提交失败");
        }
        return key.longValue();
    }

    @Override
    public PageResult<AppealVo> listMine(Long userId, int pageNum, int pageSize) {
        // 查询总数
        String countSql = "SELECT COUNT(1) FROM credit_appeal WHERE appellant_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, userId);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 分页查询
        String listSql = "SELECT * FROM credit_appeal WHERE appellant_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, userId, pageSize, offset);

        // 收集违规ID，批量查询违规类型
        List<Long> violationIds = rows.stream()
                .map(r -> ((Number) r.get("violation_id")).longValue())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> violationTypeMap = batchQueryViolationTypes(violationIds);

        // 映射为VO
        List<AppealVo> vos = rows.stream().map(r -> {
            AppealVo vo = new AppealVo();
            vo.setId(((Number) r.get("id")).longValue());
            vo.setViolationId(((Number) r.get("violation_id")).longValue());
            vo.setReason((String) r.get("reason"));
            vo.setEvidenceFileId((String) r.get("evidence_file_id"));
            vo.setStatus(((Number) r.get("status")).intValue());
            vo.setStatusDesc(statusToDesc(vo.getStatus()));
            vo.setReviewRemark((String) r.get("review_remark"));

            Object reviewTime = r.get("review_time");
            if (reviewTime instanceof LocalDateTime) {
                vo.setReviewTime((LocalDateTime) reviewTime);
            } else if (reviewTime instanceof Timestamp) {
                vo.setReviewTime(((Timestamp) reviewTime).toLocalDateTime());
            }

            Object createTime = r.get("create_time");
            if (createTime instanceof LocalDateTime) {
                vo.setCreateTime((LocalDateTime) createTime);
            } else if (createTime instanceof Timestamp) {
                vo.setCreateTime(((Timestamp) createTime).toLocalDateTime());
            }

            // 关联违规类型
            Long violationId = ((Number) r.get("violation_id")).longValue();
            vo.setViolationType(violationTypeMap.get(violationId));

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(total, vos, pageNum, pageSize);
    }

    /**
     * 将状态码转为描述
     */
    private String statusToDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "未知";
        }
    }

    /**
     * 批量查询违规类型
     */
    private Map<Long, String> batchQueryViolationTypes(List<Long> violationIds) {
        if (violationIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        String placeholders = violationIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, type FROM credit_violation WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, violationIds.toArray());
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> (String) r.get("type")
        ));
    }
}
