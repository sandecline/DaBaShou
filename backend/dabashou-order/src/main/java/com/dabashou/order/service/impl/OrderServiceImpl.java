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
        order.setBuyerVerifyCode(generateVerifyCode());
        order.setSellerVerifyCode(generateVerifyCode());
        order.setBuyerVerified(0);
        order.setSellerVerified(0);
        order.setBuyerConfirmed(0);
        order.setSellerConfirmed(0);
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
        Long shelfId = normalizeOptionalId(dto.getShelfId());
        claimDemand(dto.getDemandId());
        Map<String, Object> shelf = queryShelfInfo(shelfId);
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
        order.setSkillShelfId(shelfId);
        order.setSkillTagId(tagId);
        order.setTitle(title);
        order.setPointAmount(pointAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setRemark(dto.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setBuyerVerifyCode(generateVerifyCode());
        order.setSellerVerifyCode(generateVerifyCode());
        order.setBuyerVerified(0);
        order.setSellerVerified(0);
        order.setBuyerConfirmed(0);
        order.setSellerConfirmed(0);
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

        // 恢复货架状态为上架（仅当货架仍处于被订单占用的下架状态时）
        if (order.getSkillShelfId() != null) {
            int restored = jdbcTemplate.update(
                    "UPDATE dbs_skill_shelf SET status = 1, update_time = NOW() WHERE id = ? AND status = 0",
                    order.getSkillShelfId());
            if (restored > 0) {
                log.info("订单取消，恢复货架状态: shelfId={}", order.getSkillShelfId());
            }
        }

        // 恢复需求状态为开放
        if (order.getDemandId() != null) {
            jdbcTemplate.update(
                    "UPDATE dbs_demand SET status = 1, update_time = NOW() WHERE id = ?",
                    order.getDemandId());
            log.info("订单取消，恢复需求状态: demandId={}", order.getDemandId());
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
        checkOrderParticipant(userId, order);
        String phase = dto.getPhase();
        boolean isBuyer = userId.equals(order.getBuyerId());

        if ("start".equals(phase)) {
            verifyStartPhase(order, dto.getCode(), isBuyer);
        } else if ("complete".equals(phase)) {
            verifyCompletePhase(order, dto.getCode(), isBuyer);
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的核销阶段: " + phase);
        }
    }

    /**
     * 开始核销阶段（1→3）
     * 双方输入对方的开始核销码，双方都核销后冻结积分、生成确认码、转入服务中
     */
    private void verifyStartPhase(Order order, String code, boolean isBuyer) {
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_PAYMENT.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单不在待核销状态");
        }

        // 校验当前用户是否已核销
        if (isBuyer && Objects.equals(order.getBuyerVerified(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已完成核销，请等待对方");
        }
        if (!isBuyer && Objects.equals(order.getSellerVerified(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已完成核销，请等待对方");
        }

        // 校验核销码：买家输入卖家的码，卖家输入买家的码
        String expectedCode = isBuyer ? order.getSellerVerifyCode() : order.getBuyerVerifyCode();
        if (expectedCode == null || !expectedCode.equals(code)) {
            throw new BusinessException(ErrorCode.CONFLICT, "核销码错误");
        }

        // 记录核销状态
        LocalDateTime now = LocalDateTime.now();
        if (isBuyer) {
            order.setBuyerVerified(1);
        } else {
            order.setSellerVerified(1);
        }
        order.setUpdateTime(now);

        // 双方都核销了 → 转入服务中
        if (Objects.equals(order.getBuyerVerified(), 1) && Objects.equals(order.getSellerVerified(), 1)) {
            pointService.freeze(order.getBuyerId(), order.getPointAmount(), order.getId());
            order.setBuyerConfirmCode(generateVerifyCode());
            order.setSellerConfirmCode(generateVerifyCode());
            order.setBuyerConfirmed(0);
            order.setSellerConfirmed(0);
            order.setStatus(OrderStatus.IN_SERVICE.getCode());
            order.setServiceStartTime(now);
            log.info("订单开始核销完成，转入服务中: orderId={}", order.getId());
        } else {
            log.info("订单单方核销: orderId={}, isBuyer={}", order.getId(), isBuyer);
        }

        updateById(order);
    }

    /**
     * 完成确认阶段（3→5）
     * 双方输入对方的完成确认码，双方都确认后结算积分、完成订单
     */
    private void verifyCompletePhase(Order order, String code, boolean isBuyer) {
        if (!Objects.equals(order.getStatus(), OrderStatus.IN_SERVICE.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单不在服务中状态");
        }

        // 校验当前用户是否已确认
        if (isBuyer && Objects.equals(order.getBuyerConfirmed(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已确认完成，请等待对方");
        }
        if (!isBuyer && Objects.equals(order.getSellerConfirmed(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已确认完成，请等待对方");
        }

        // 校验确认码：买家输入卖家的码，卖家输入买家的码
        String expectedCode = isBuyer ? order.getSellerConfirmCode() : order.getBuyerConfirmCode();
        if (expectedCode == null || !expectedCode.equals(code)) {
            throw new BusinessException(ErrorCode.CONFLICT, "确认码错误");
        }

        // 记录确认状态
        LocalDateTime now = LocalDateTime.now();
        if (isBuyer) {
            order.setBuyerConfirmed(1);
        } else {
            order.setSellerConfirmed(1);
        }
        order.setUpdateTime(now);

        // 双方都确认了 → 完成订单
        if (Objects.equals(order.getBuyerConfirmed(), 1) && Objects.equals(order.getSellerConfirmed(), 1)) {
            pointService.settle(order.getId());
            order.setStatus(OrderStatus.COMPLETED.getCode());
            order.setCompleteTime(now);
            log.info("订单双方确认完成: orderId={}", order.getId());
        } else {
            log.info("订单单方确认: orderId={}, isBuyer={}", order.getId(), isBuyer);
        }

        updateById(order);
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
        // 服务中(3)和已完成(5)都可以发起争议
        if (!Objects.equals(order.getStatus(), OrderStatus.IN_SERVICE.getCode())
                && !Objects.equals(order.getStatus(), OrderStatus.COMPLETED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许发起争议");
        }
        order.setStatus(OrderStatus.DISPUTING.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单争议: orderId={}, reason={}, byUserId={}", orderId, dto.getReason(), userId);
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
    public int autoConfirmTimeout() {
        // 查询系统配置的确认超时时间
        Integer timeoutHours = 72; // 默认72小时
        try {
            String val = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM sys_config WHERE config_key = 'order.confirm_timeout_hours'", String.class);
            if (val != null) timeoutHours = Integer.parseInt(val);
        } catch (Exception ignored) {
        }

        // 找到超时的待确认订单
        List<Map<String, Object>> overdue = jdbcTemplate.queryForList(
                "SELECT id FROM dbs_order WHERE status = 4 AND service_end_time < DATE_SUB(NOW(), INTERVAL ? HOUR)",
                timeoutHours);

        int count = 0;
        for (Map<String, Object> row : overdue) {
            Long orderId = ((Number) row.get("id")).longValue();
            try {
                pointService.settle(orderId);
                jdbcTemplate.update(
                        "UPDATE dbs_order SET status = 5, complete_time = NOW(), update_time = NOW() WHERE id = ? AND status = 4",
                        orderId);
                count++;
                log.info("自动确认完成: orderId={}", orderId);
            } catch (Exception e) {
                log.warn("自动确认失败: orderId={}, error={}", orderId, e.getMessage());
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long userId, Long orderId, RefundDto dto) {
        Order order = getByIdOrThrow(orderId);
        checkOrderParticipant(userId, order);
        if (!Objects.equals(order.getStatus(), OrderStatus.IN_SERVICE.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许退款操作");
        }

        boolean isBuyer = userId.equals(order.getBuyerId());
        String role = isBuyer ? "buyer" : "seller";

        if (order.getRefundRequester() == null) {
            // 首次发起退款
            order.setRefundRequester(role);
            order.setRefundAgreed(0);
            order.setUpdateTime(LocalDateTime.now());
            updateById(order);
            log.info("退款申请已发起: orderId={}, requester={}", orderId, role);
        } else if (!role.equals(order.getRefundRequester())) {
            // 对方同意退款 → 执行退款
            pointService.refund(orderId);
            order.setStatus(OrderStatus.REFUNDED.getCode());
            order.setRefundAgreed(1);
            order.setUpdateTime(LocalDateTime.now());
            updateById(order);
            log.info("退款双方同意，已执行: orderId={}", orderId);
        } else {
            throw new BusinessException(ErrorCode.CONFLICT, "您已发起退款申请，请等待对方同意");
        }
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
        vo.setBuyerVerifyCode(order.getBuyerVerifyCode());
        vo.setSellerVerifyCode(order.getSellerVerifyCode());
        vo.setBuyerConfirmCode(order.getBuyerConfirmCode());
        vo.setSellerConfirmCode(order.getSellerConfirmCode());
        vo.setBuyerVerified(order.getBuyerVerified() != null && order.getBuyerVerified() == 1);
        vo.setSellerVerified(order.getSellerVerified() != null && order.getSellerVerified() == 1);
        vo.setBuyerConfirmed(order.getBuyerConfirmed() != null && order.getBuyerConfirmed() == 1);
        vo.setSellerConfirmed(order.getSellerConfirmed() != null && order.getSellerConfirmed() == 1);
        vo.setRefundRequester(order.getRefundRequester());
        vo.setRefundAgreed(order.getRefundAgreed() != null && order.getRefundAgreed() == 1);
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

    private Long normalizeOptionalId(Long id) {
        return id == null || id <= 0 ? null : id;
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
