package com.dabashou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.OrderStatus;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.order.domain.Order;
import com.dabashou.order.dto.*;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.order.service.OrderService;
import com.dabashou.order.vo.*;
import com.dabashou.point.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现 — 订单状态机闭环
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String ORDER_VERIFY_KEY_PREFIX = "order:verify:";
    private static final String ORDER_CREATE_IDEM_PREFIX = "order:create:idem:";
    private static final String ORDER_PAY_IDEM_PREFIX = "order:pay:idem:";
    private static final long VERIFY_CODE_TTL_MINUTES = 30;
    private static final long IDEM_TTL_MINUTES = 5;

    private final PointService pointService;
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public OrderServiceImpl(PointService pointService, JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.pointService = pointService;
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromShelf(Long userId, CreateOrderDto dto) {
        if (!markIdempotent(ORDER_CREATE_IDEM_PREFIX, dto.getIdempotentToken())) {
            throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
        }
        Map<String, Object> shelf = queryShelfInfo(dto.getShelfId());
        if (shelf == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在: " + dto.getShelfId());
        }
        Number statusNum = (Number) shelf.get("status");
        if (statusNum == null || statusNum.intValue() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "货架已下架或审核中");
        }
        Long sellerId = ((Number) shelf.get("user_id")).longValue();
        if (userId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能购买自己的服务");
        }
        claimShelf(dto.getShelfId());
        Long tagId = shelf.get("skill_tag_id") != null ? ((Number) shelf.get("skill_tag_id")).longValue() : null;
        String title = (String) shelf.get("title");
        Integer pointAmount = ((Number) shelf.get("point_price")).intValue();

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(sellerId);
        order.setSkillShelfId(dto.getShelfId());
        order.setSkillTagId(tagId);
        order.setTitle(title);
        order.setPointAmount(pointAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTimeSlotId(dto.getTimeSlotId());
        order.setRemark(dto.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("创建订单: orderId={}, buyerId={}, sellerId={}, shelfId={}", order.getId(), userId, sellerId, dto.getShelfId());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromDemand(Long userId, CreateOrderFromDemandDto dto) {
        if (!markIdempotent(ORDER_CREATE_IDEM_PREFIX, dto.getIdempotentToken())) {
            throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
        }
        Map<String, Object> demand = queryDemandInfo(dto.getDemandId());
        if (demand == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在: " + dto.getDemandId());
        }
        Number demandStatus = (Number) demand.get("status");
        if (demandStatus == null || demandStatus.intValue() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求已关闭或不在待接单状态");
        }
        Long buyerId = ((Number) demand.get("user_id")).longValue();
        if (userId.equals(buyerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能接自己的需求");
        }
        claimDemand(dto.getDemandId());
        Map<String, Object> shelf = queryShelfInfo(dto.getShelfId());
        Long tagId;
        String title;
        if (shelf != null) {
            Number shelfStatus = (Number) shelf.get("status");
            if (shelfStatus != null && shelfStatus.intValue() == 1) {
                tagId = shelf.get("skill_tag_id") != null ? ((Number) shelf.get("skill_tag_id")).longValue() : null;
                title = (String) shelf.get("title");
            } else {
                tagId = demand.get("skill_tag_id") != null ? ((Number) demand.get("skill_tag_id")).longValue() : null;
                title = (String) demand.get("title");
            }
        } else {
            tagId = demand.get("skill_tag_id") != null ? ((Number) demand.get("skill_tag_id")).longValue() : null;
            title = (String) demand.get("title");
        }
        Integer pointAmount = ((Number) demand.get("point_reward")).intValue();

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(userId);
        order.setDemandId(dto.getDemandId());
        order.setSkillShelfId(dto.getShelfId());
        order.setSkillTagId(tagId);
        order.setTitle(title);
        order.setPointAmount(pointAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setRemark(dto.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("从需求创建订单: orderId={}, buyerId={}, sellerId={}, demandId={}", order.getId(), buyerId, userId, dto.getDemandId());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVo payOrder(Long userId, Long orderId, String idempotentToken) {
        if (!markIdempotent(ORDER_PAY_IDEM_PREFIX, idempotentToken)) {
            throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
        }
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.PAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许支付: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        pointService.freeze(order.getBuyerId(), order.getPointAmount(), order.getId());
        String verifyCode = generateVerifyCode();
        setVerifyCodeToRedis(orderId, verifyCode);
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.PAID.getCode());
        order.setVerifyCode(verifyCode);
        order.setVerifyCodeExpire(now.plusMinutes(VERIFY_CODE_TTL_MINUTES));
        order.setUpdateTime(now);
        updateById(order);
        log.info("订单支付成功: orderId={}", orderId);
        PayResultVo vo = new PayResultVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPointAmount(order.getPointAmount());
        vo.setVerifyCode(verifyCode);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, CancelDto dto) {
        Order order = getByIdOrThrow(orderId);
        checkOrderParticipant(userId, order);
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.CANCELLED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许取消: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        if (Objects.equals(order.getStatus(), OrderStatus.PAID.getCode())
                || Objects.equals(order.getStatus(), OrderStatus.IN_SERVICE.getCode())) {
            pointService.unfreeze(orderId);
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(dto.getReason());
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单取消: orderId={}, reason={}", orderId, dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.IN_SERVICE.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许开始服务: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        order.setStatus(OrderStatus.IN_SERVICE.getCode());
        order.setServiceStartTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单开始服务: orderId={}", orderId);
    }

    @Override
    public VerifyCodeVo getVerifyCode(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        String verifyCode = getVerifyCodeFromRedis(orderId);
        LocalDateTime expireTime = order.getVerifyCodeExpire();
        if (verifyCode == null && order.getVerifyCode() != null) {
            verifyCode = order.getVerifyCode();
            if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
                setVerifyCodeToRedis(orderId, verifyCode);
            }
        }
        VerifyCodeVo vo = new VerifyCodeVo();
        vo.setVerifyCode(verifyCode);
        vo.setExpireTime(expireTime);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyCodeVo refreshVerifyCode(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!Objects.equals(order.getStatus(), OrderStatus.PAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许刷新核销码");
        }
        String newCode = generateVerifyCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(VERIFY_CODE_TTL_MINUTES);
        order.setVerifyCode(newCode);
        order.setVerifyCodeExpire(expireTime);
        order.setUpdateTime(now);
        updateById(order);
        setVerifyCodeToRedis(orderId, newCode);
        VerifyCodeVo vo = new VerifyCodeVo();
        vo.setVerifyCode(newCode);
        vo.setExpireTime(expireTime);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOrder(Long userId, Long orderId, VerifyDto dto) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.PENDING_CONFIRM.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许核销: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        String verifyCode = getVerifyCodeFromRedis(orderId);
        if (verifyCode == null && order.getVerifyCode() != null
                && order.getVerifyCodeExpire() != null
                && order.getVerifyCodeExpire().isAfter(LocalDateTime.now())) {
            verifyCode = order.getVerifyCode();
        }
        if (verifyCode == null || !verifyCode.equals(dto.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "核销码无效或已过期");
        }
        order.setStatus(OrderStatus.PENDING_CONFIRM.getCode());
        order.setServiceEndTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        try {
            redisTemplate.delete(ORDER_VERIFY_KEY_PREFIX + orderId);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis不可用，跳过核销码缓存删除: {}", e.getMessage());
        }
        log.info("订单核销: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.COMPLETED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许确认: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        pointService.settle(orderId);
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单确认完成: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disputeOrder(Long userId, Long orderId, DisputeDto dto) {
        Order order = getByIdOrThrow(orderId);
        checkOrderParticipant(userId, order);
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.DISPUTING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许争议: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        order.setStatus(OrderStatus.DISPUTING.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单争议: orderId={}, reason={}", orderId, dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arbitrateOrder(Long orderId, ArbitrateDto dto) {
        Order order = getByIdOrThrow(orderId);
        if (!Objects.equals(order.getStatus(), OrderStatus.DISPUTING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单非争议状态，无法仲裁");
        }
        if (dto.getRefundAmount() != null && dto.getRefundAmount() > 0) {
            pointService.refund(orderId);
            order.setStatus(OrderStatus.REFUNDED.getCode());
        } else {
            pointService.settle(orderId);
            order.setStatus(OrderStatus.COMPLETED.getCode());
        }
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单仲裁: orderId={}, result={}", orderId, dto.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long userId, Long orderId, RefundDto dto) {
        Order order = getByIdOrThrow(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.REFUNDED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许退款: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        pointService.refund(orderId);
        order.setStatus(OrderStatus.REFUNDED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单退款: orderId={}, reason={}", orderId, dto.getReason());
    }

    @Override
    public OrderDetailVo getOrderDetail(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        checkOrderParticipantReadOnly(userId, order);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setShelfId(order.getSkillShelfId());
        vo.setDemandId(order.getDemandId());
        vo.setPointAmount(order.getPointAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(OrderStatus.ofCode(order.getStatus()).getDesc());
        vo.setVerifyCode(order.getVerifyCode());
        vo.setVerifyCodeExpire(order.getVerifyCodeExpire());
        vo.setTimeSlotId(order.getTimeSlotId());
        vo.setServiceStartTime(order.getServiceStartTime());
        vo.setServiceEndTime(order.getServiceEndTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        Map<String, Object> buyerInfo = queryUserInfo(order.getBuyerId());
        if (buyerInfo != null) {
            vo.setBuyerNickname((String) buyerInfo.get("nickname"));
            vo.setBuyerAvatar((String) buyerInfo.get("avatar"));
        }
        Map<String, Object> sellerInfo = queryUserInfo(order.getSellerId());
        if (sellerInfo != null) {
            vo.setSellerNickname((String) sellerInfo.get("nickname"));
            vo.setSellerAvatar((String) sellerInfo.get("avatar"));
        }
        vo.setShelfTitle(order.getTitle());
        vo.setTagName(queryTagName(order.getSkillTagId()));
        return vo;
    }

    @Override
    public OrderStatusVo getOrderStatus(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        checkOrderParticipantReadOnly(userId, order);
        OrderStatus status = OrderStatus.ofCode(order.getStatus());
        return new OrderStatusVo(status.getCode(), status.getDesc());
    }

    @Override
    public PageResult<OrderItemVo> listOrders(Long userId, String role, Integer status, int pageNum, int pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId));
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = page(page, wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResult.of(result.getTotal(), List.of(), pageNum, pageSize);
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> tagIds = new HashSet<>();
        for (Order order : result.getRecords()) {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
            if (order.getSkillTagId() != null) {
                tagIds.add(order.getSkillTagId());
            }
        }
        Map<Long, String> nicknameMap = batchQueryNicknames(userIds);
        Map<Long, String> tagNameMap = batchQueryTagNames(tagIds);

        List<OrderItemVo> list = new ArrayList<>();
        for (Order order : result.getRecords()) {
            list.add(toOrderItemVo(order, nicknameMap, tagNameMap));
        }
        return PageResult.of(result.getTotal(), list, pageNum, pageSize);
    }

    // ========== 私有方法 ==========

    private Order getByIdOrThrow(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在: " + orderId);
        }
        return order;
    }

    private void checkOrderParticipant(Long userId, Order order) {
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
    }

    private void checkOrderParticipantReadOnly(Long userId, Order order) {
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
    }

    private Map<String, Object> queryShelfInfo(Long shelfId) {
        if (shelfId == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT user_id, skill_tag_id, title, point_price, status FROM dbs_skill_shelf WHERE id = ?",
                    shelfId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void claimDemand(Long demandId) {
        int updated = jdbcTemplate.update(
                "UPDATE dbs_demand SET status = 2, update_time = ? WHERE id = ? AND status = 1",
                LocalDateTime.now(), demandId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求已被接单或不在待接单状态");
        }
    }

    private void claimShelf(Long shelfId) {
        int updated = jdbcTemplate.update(
                "UPDATE dbs_skill_shelf SET status = 0, update_time = ? WHERE id = ? AND status = 1",
                LocalDateTime.now(), shelfId);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "服务已被接取或不在上架状态");
        }
    }

    private Map<String, Object> queryDemandInfo(Long demandId) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT user_id, skill_tag_id, title, point_reward, status FROM dbs_demand WHERE id = ?",
                    demandId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Map<String, Object> queryUserInfo(Long userId) {
        try {
            return jdbcTemplate.queryForMap("SELECT nickname, avatar FROM dbs_user WHERE id = ?", userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private String queryTagName(Long tagId) {
        if (tagId == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject("SELECT name FROM dbs_skill_tag WHERE id = ?", String.class, tagId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Map<Long, String> batchQueryNicknames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT id, nickname FROM dbs_user WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userIds.toArray());
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> (String) r.get("nickname"),
                (a, b) -> a));
    }

    private Map<Long, String> batchQueryTagNames(Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(tagIds.size(), "?"));
        String sql = "SELECT id, name FROM dbs_skill_tag WHERE id IN (" + placeholders + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tagIds.toArray());
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> (String) r.get("name"),
                (a, b) -> a));
    }

    private OrderItemVo toOrderItemVo(Order order, Map<Long, String> nicknameMap, Map<Long, String> tagNameMap) {
        OrderItemVo vo = new OrderItemVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setBuyerNickname(nicknameMap.get(order.getBuyerId()));
        vo.setSellerId(order.getSellerId());
        vo.setSellerNickname(nicknameMap.get(order.getSellerId()));
        vo.setShelfTitle(order.getTitle());
        vo.setTagName(tagNameMap.get(order.getSkillTagId()));
        vo.setPointAmount(order.getPointAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(OrderStatus.ofCode(order.getStatus()).getDesc());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private String getVerifyCodeFromRedis(Long orderId) {
        try {
            return redisTemplate.opsForValue().get(ORDER_VERIFY_KEY_PREFIX + orderId);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis不可用，读取核销码缓存失败: {}", e.getMessage());
            return null;
        }
    }

    private void setVerifyCodeToRedis(Long orderId, String code) {
        try {
            redisTemplate.opsForValue().set(ORDER_VERIFY_KEY_PREFIX + orderId, code,
                    VERIFY_CODE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis不可用，跳过核销码缓存写入: {}", e.getMessage());
        }
    }

    private boolean markIdempotent(String prefix, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(prefix + token, "1",
                    IDEM_TTL_MINUTES, TimeUnit.MINUTES));
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis不可用，跳过幂等缓存校验: {}", e.getMessage());
            return true;
        }
    }

    private String generateOrderNo() {
        return "DBS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private String generateVerifyCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
