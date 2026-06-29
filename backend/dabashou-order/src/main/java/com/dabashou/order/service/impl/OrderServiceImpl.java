package com.dabashou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.OrderStatus;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.demand.api.DemandApi;
import com.dabashou.order.domain.Order;
import com.dabashou.order.dto.*;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.order.service.OrderService;
import com.dabashou.order.vo.*;
import com.dabashou.point.api.PointApi;
import com.dabashou.shelf.api.ShelfApi;
import com.dabashou.user.api.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现 — 跨模块真实调用 + Redis幂等/核销码
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String VERIFY_CODE_KEY = "dbs:verify:";
    private static final String IDEMPOTENT_KEY = "dbs:idem:";
    private static final long VERIFY_CODE_TTL_MINUTES = 30;
    private static final long IDEMPOTENT_TTL_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final ShelfApi shelfApi;
    private final DemandApi demandApi;
    private final PointApi pointApi;
    private final UserApi userApi;

    public OrderServiceImpl(StringRedisTemplate redisTemplate,
                            ShelfApi shelfApi, DemandApi demandApi,
                            PointApi pointApi, UserApi userApi) {
        this.redisTemplate = redisTemplate;
        this.shelfApi = shelfApi;
        this.demandApi = demandApi;
        this.pointApi = pointApi;
        this.userApi = userApi;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromShelf(Long userId, CreateOrderDto dto) {
        Long shelfId = dto.getSkillShelfId();
        if (!shelfApi.isOnShelf(shelfId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在或已下架");
        }
        Long sellerId = shelfApi.getUserId(shelfId);
        if (sellerId.equals(userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能购买自己的服务");
        }
        Integer pointPrice = shelfApi.getPointPrice(shelfId);
        Long tagId = shelfApi.getSkillTagId(shelfId);
        String shelfTitle = shelfApi.getTitle(shelfId);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(sellerId);
        order.setSkillShelfId(shelfId);
        order.setSkillTagId(tagId);
        order.setTitle(shelfTitle);
        order.setPointAmount(pointPrice);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTimeSlotId(dto.getTimeSlotId());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("创建订单: orderId={}, buyerId={}, sellerId={}, shelfId={}, amount={}",
                order.getId(), userId, sellerId, shelfId, pointPrice);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromDemand(Long userId, CreateOrderFromDemandDto dto) {
        Long demandId = dto.getDemandId();
        Integer pointReward = demandApi.getPointReward(demandId);
        Long tagId = demandApi.getSkillTagId(demandId);
        String title = demandApi.getTitle(demandId);
        Long demandOwnerId = demandApi.getUserId(demandId);

        if (demandOwnerId.equals(userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能接自己的需求");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(demandOwnerId);
        order.setSellerId(userId);
        order.setDemandId(demandId);
        order.setSkillTagId(tagId);
        order.setTitle(title);
        order.setPointAmount(pointReward != null ? pointReward : 0);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        // 更新需求状态为已接单
        demandApi.updateStatus(demandId, 2);
        log.info("从需求创建订单: orderId={}, demandId={}, buyerId={}, sellerId={}",
                order.getId(), demandId, demandOwnerId, userId);
        return order.getId();
    }

    @Override
    public PageResult<OrderItemVo> listOrders(Long userId, String role, Integer status, int pageNum, int pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.eq(Order::getBuyerId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        var result = page(page, wrapper);

        List<OrderItemVo> items = result.getRecords().stream().map(order -> {
            OrderItemVo vo = new OrderItemVo();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setBuyerId(order.getBuyerId());
            vo.setSellerId(order.getSellerId());
            vo.setShelfTitle(order.getTitle());
            vo.setPointAmount(order.getPointAmount());
            vo.setStatus(order.getStatus());
            vo.setStatusName(OrderStatus.ofCode(order.getStatus()).getDesc());
            vo.setCreateTime(order.getCreateTime());
            vo.setBuyerNickname(userApi.getNickname(order.getBuyerId()));
            vo.setSellerNickname(userApi.getNickname(order.getSellerId()));
            return vo;
        }).toList();

        return PageResult.of(result.getTotal(), items, pageNum, pageSize);
    }

    @Override
    public OrderDetailVo getOrderDetail(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        checkParticipant(userId, order);

        OrderDetailVo vo = new OrderDetailVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setShelfId(order.getSkillShelfId());
        vo.setDemandId(order.getDemandId());
        vo.setShelfTitle(order.getTitle());
        vo.setPointAmount(order.getPointAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(OrderStatus.ofCode(order.getStatus()).getDesc());
        vo.setVerifyCode(order.getVerifyCode());
        vo.setVerifyCodeExpire(order.getVerifyCodeExpire());
        vo.setTimeSlotId(order.getTimeSlotId());
        vo.setServiceStartTime(order.getServiceStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreateTime(order.getCreateTime());
        vo.setBuyerNickname(userApi.getNickname(order.getBuyerId()));
        vo.setSellerNickname(userApi.getNickname(order.getSellerId()));
        vo.setBuyerAvatar(userApi.getAvatar(order.getBuyerId()));
        vo.setSellerAvatar(userApi.getAvatar(order.getSellerId()));
        return vo;
    }

    @Override
    public OrderStatusVo getOrderStatus(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        checkParticipant(userId, order);
        OrderStatus status = OrderStatus.ofCode(order.getStatus());
        return new OrderStatusVo(status.getCode(), status.getDesc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVo payOrder(Long userId, Long orderId, String idempotentToken) {
        // 幂等校验
        String idemKey = IDEMPOTENT_KEY + idempotentToken;
        Boolean firstTime = redisTemplate.opsForValue().setIfAbsent(idemKey, "1", IDEMPOTENT_TTL_MINUTES, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(firstTime)) {
            throw new BusinessException(ErrorCode.CONFLICT, "重复支付请求");
        }

        Order order = getByIdOrThrow(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以支付");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.PAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许支付: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }

        // 冻结买家积分
        pointApi.freeze(userId, orderId, order.getPointAmount(), "订单支付冻结: " + order.getOrderNo());

        // 生成核销码存Redis
        String verifyCode = generateVerifyCode();
        redisTemplate.opsForValue().set(VERIFY_CODE_KEY + orderId, verifyCode, VERIFY_CODE_TTL_MINUTES, TimeUnit.MINUTES);

        order.setStatus(OrderStatus.PAID.getCode());
        order.setVerifyCode(verifyCode);
        order.setVerifyCodeExpire(LocalDateTime.now().plusMinutes(VERIFY_CODE_TTL_MINUTES));
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        log.info("订单支付成功: orderId={}, verifyCode={}", orderId, verifyCode);
        PayResultVo vo = new PayResultVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPointAmount(order.getPointAmount());
        vo.setVerifyCode(verifyCode);
        vo.setVerifyCodeExpire(order.getVerifyCodeExpire());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, CancelDto dto) {
        Order order = getByIdOrThrow(orderId);
        checkParticipant(userId, order);
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.CANCELLED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许取消: " + current.getDesc());
        }
        // 如果已支付，退款解冻
        if (current == OrderStatus.PAID) {
            pointApi.refundFrozen(order.getBuyerId(), orderId, order.getPointAmount(),
                    "订单取消退款: " + order.getOrderNo());
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
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以开始服务");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.IN_SERVICE.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许开始服务: " + OrderStatus.ofCode(order.getStatus()).getDesc());
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
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以查看核销码");
        }
        VerifyCodeVo vo = new VerifyCodeVo();
        vo.setVerifyCode(order.getVerifyCode());
        vo.setExpireTime(order.getVerifyCodeExpire());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyCodeVo refreshVerifyCode(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以刷新核销码");
        }
        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有已支付订单可以刷新核销码");
        }
        String newCode = generateVerifyCode();
        redisTemplate.opsForValue().set(VERIFY_CODE_KEY + orderId, newCode, VERIFY_CODE_TTL_MINUTES, TimeUnit.MINUTES);
        order.setVerifyCode(newCode);
        order.setVerifyCodeExpire(LocalDateTime.now().plusMinutes(VERIFY_CODE_TTL_MINUTES));
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        VerifyCodeVo vo = new VerifyCodeVo();
        vo.setVerifyCode(newCode);
        vo.setExpireTime(order.getVerifyCodeExpire());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOrder(Long userId, Long orderId, VerifyDto dto) {
        Order order = getByIdOrThrow(orderId);
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以核销");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.PENDING_CONFIRM.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许核销: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        // 校验核销码
        String cachedCode = redisTemplate.opsForValue().get(VERIFY_CODE_KEY + orderId);
        if (cachedCode == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "核销码已过期，请买家刷新");
        }
        if (!cachedCode.equals(dto.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "核销码错误");
        }
        redisTemplate.delete(VERIFY_CODE_KEY + orderId);
        order.setStatus(OrderStatus.PENDING_CONFIRM.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单核销: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以确认完成");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.COMPLETED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许确认: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        // 结算: 解冻买家积分→转给卖家
        pointApi.unfreezeAndTransfer(order.getBuyerId(), order.getSellerId(), orderId,
                order.getPointAmount(), "订单结算: " + order.getOrderNo());
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
        checkParticipant(userId, order);
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.DISPUTING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许争议: " + OrderStatus.ofCode(order.getStatus()).getDesc());
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
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (current != OrderStatus.DISPUTING) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单非争议状态，无法仲裁");
        }
        if (dto.getRefundAmount() != null && dto.getRefundAmount() > 0) {
            // 退款给买家
            pointApi.refundFrozen(order.getBuyerId(), orderId, dto.getRefundAmount(),
                    "仲裁退款: " + order.getOrderNo());
            // 剩余转给卖家
            int remaining = order.getPointAmount() - dto.getRefundAmount();
            if (remaining > 0) {
                pointApi.reward(order.getSellerId(), remaining, "仲裁结算: " + order.getOrderNo());
            }
            order.setStatus(OrderStatus.REFUNDED.getCode());
        } else {
            // 全额结算给卖家
            pointApi.unfreezeAndTransfer(order.getBuyerId(), order.getSellerId(), orderId,
                    order.getPointAmount(), "仲裁结算: " + order.getOrderNo());
            order.setStatus(OrderStatus.COMPLETED.getCode());
        }
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单仲裁: orderId={}, result={}, refundAmount={}", orderId, dto.getResult(), dto.getRefundAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long userId, Long orderId, RefundDto dto) {
        Order order = getByIdOrThrow(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以申请退款");
        }
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.REFUNDED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许退款: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        pointApi.refundFrozen(order.getBuyerId(), orderId, order.getPointAmount(),
                "订单退款: " + order.getOrderNo());
        order.setStatus(OrderStatus.REFUNDED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单退款: orderId={}, reason={}", orderId, dto.getReason());
    }

    // ========== 私有方法 ==========

    private Order getByIdOrThrow(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在: " + orderId);
        }
        return order;
    }

    private void checkParticipant(Long userId, Order order) {
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
    }

    private String generateOrderNo() {
        return "DBS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateVerifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
