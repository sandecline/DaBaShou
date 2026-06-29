package com.dabashou.credit.service.impl;

import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.mapper.ReviewMapper;
import com.dabashou.credit.service.ReviewService;
import com.dabashou.credit.vo.ReviewVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final JdbcTemplate jdbcTemplate;

    public ReviewServiceImpl(ReviewMapper reviewMapper, JdbcTemplate jdbcTemplate) {
        this.reviewMapper = reviewMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Long userId, ReviewDto dto) {
        // 查询订单信息
        String orderSql = "SELECT buyer_id, seller_id, status FROM dbs_order WHERE id = ?";
        Map<String, Object> orderRow;
        try {
            orderRow = jdbcTemplate.queryForMap(orderSql, dto.getOrderId());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }

        Long buyerId = ((Number) orderRow.get("buyer_id")).longValue();
        Long sellerId = ((Number) orderRow.get("seller_id")).longValue();
        Integer status = ((Number) orderRow.get("status")).intValue();

        // 校验订单已完成后才能评价
        if (status != 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单未完成，暂不能评价");
        }

        // 校验当前用户是订单参与者
        if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您不是该订单的参与者");
        }

        // 确定被评价方
        Long revieweeId = userId.equals(buyerId) ? sellerId : buyerId;

        // 校验唯一约束：同一订单同一用户只能评价一次
        String checkSql = "SELECT COUNT(1) FROM dbs_review WHERE order_id = ? AND reviewer_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, dto.getOrderId(), userId);
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已评价过该订单");
        }

        // 处理图片
        String images = null;
        if (dto.getImages() != null && dto.getImages().length > 0) {
            images = String.join(",", dto.getImages());
        }

        int isAnonymous = Boolean.TRUE.equals(dto.getIsAnonymous()) ? 1 : 0;

        // 插入评价
        String insertSql = "INSERT INTO dbs_review (order_id, reviewer_id, reviewee_id, rating, content, images, is_anonymous, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, dto.getOrderId());
            ps.setLong(2, userId);
            ps.setLong(3, revieweeId);
            ps.setInt(4, dto.getRating());
            ps.setString(5, dto.getContent());
            ps.setString(6, images);
            ps.setInt(7, isAnonymous);
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "评价提交失败");
        }
        return key.longValue();
    }

    @Override
    public PageResult<ReviewVo> listMine(Long userId, int pageNum, int pageSize) {
        // 查询总数
        String countSql = "SELECT COUNT(1) FROM dbs_review WHERE reviewer_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, userId);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 分页查询
        String listSql = "SELECT * FROM dbs_review WHERE reviewer_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, userId, pageSize, offset);

        // 收集被评价方用户ID
        List<Long> revieweeIds = rows.stream()
                .map(r -> ((Number) r.get("reviewee_id")).longValue())
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, Map<String, Object>> userMap = batchQueryUsers(revieweeIds);

        // 映射为VO
        List<ReviewVo> vos = rows.stream().map(r -> mapToReviewVo(r, userMap)).collect(Collectors.toList());
        return PageResult.of(total, vos, pageNum, pageSize);
    }

    @Override
    public PageResult<ReviewVo> listReceived(Long userId, int pageNum, int pageSize) {
        // 查询总数
        String countSql = "SELECT COUNT(1) FROM dbs_review WHERE reviewee_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, userId);
        if (total == null || total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 分页查询
        String listSql = "SELECT * FROM dbs_review WHERE reviewee_id = ? ORDER BY create_time DESC LIMIT ? OFFSET ?";
        int offset = (pageNum - 1) * pageSize;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, userId, pageSize, offset);

        // 收集评价方用户ID
        List<Long> reviewerIds = rows.stream()
                .map(r -> ((Number) r.get("reviewer_id")).longValue())
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, Map<String, Object>> userMap = batchQueryUsers(reviewerIds);

        // 映射为VO
        List<ReviewVo> vos = rows.stream().map(r -> {
            ReviewVo vo = mapToReviewVo(r, null);
            // 填充评价方信息
            Long reviewerId = ((Number) r.get("reviewer_id")).longValue();
            Map<String, Object> user = userMap.get(reviewerId);
            if (user != null) {
                vo.setReviewerNickname((String) user.get("nickname"));
                vo.setReviewerAvatar((String) user.get("avatar"));
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(total, vos, pageNum, pageSize);
    }

    @Override
    public ReviewVo getOrderReview(Long userId, Long orderId) {
        String sql = "SELECT * FROM dbs_review WHERE reviewer_id = ? AND order_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId, orderId);
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未评价");
        }
        return mapToReviewVo(rows.get(0), null);
    }

    /**
     * 将数据库行映射为 ReviewVo
     */
    private ReviewVo mapToReviewVo(Map<String, Object> row, Map<Long, Map<String, Object>> userMap) {
        ReviewVo vo = new ReviewVo();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setOrderId(((Number) row.get("order_id")).longValue());
        vo.setReviewerId(((Number) row.get("reviewer_id")).longValue());
        vo.setRevieweeId(((Number) row.get("reviewee_id")).longValue());
        vo.setRating(((Number) row.get("rating")).intValue());
        vo.setContent((String) row.get("content"));
        vo.setImages((String) row.get("images"));
        vo.setIsAnonymous(((Number) row.get("is_anonymous")).intValue());

        Object createTime = row.get("create_time");
        if (createTime instanceof LocalDateTime) {
            vo.setCreateTime((LocalDateTime) createTime);
        } else if (createTime instanceof Timestamp) {
            vo.setCreateTime(((Timestamp) createTime).toLocalDateTime());
        }

        // 填充被评价方信息
        if (userMap != null) {
            Long revieweeId = ((Number) row.get("reviewee_id")).longValue();
            Map<String, Object> user = userMap.get(revieweeId);
            if (user != null) {
                vo.setRevieweeNickname((String) user.get("nickname"));
                vo.setRevieweeAvatar((String) user.get("avatar"));
            }
        }

        return vo;
    }

    /**
     * 批量查询用户昵称和头像
     */
    private Map<Long, Map<String, Object>> batchQueryUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, nickname, avatar FROM dbs_user WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userIds.toArray());
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> r
        ));
    }
}
