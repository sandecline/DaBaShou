package com.dabashou.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.OrderStatus;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.order.domain.Order;
import com.dabashou.order.dto.*;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.order.service.OrderService;
import com.dabashou.order.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单服务实现 — 骨架(核心状态机校验已实现，业务逻辑TODO)
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromShelf(Long userId, CreateOrderDto dto) {
        // TODO: 校验货架存在且上架
        // TODO: 校验不能自己买自己的服务
        // TODO: 生成订单号
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        // TODO: order.setSellerId(从货架获取卖家ID)
        order.setSkillShelfId(dto.getShelfId());
        // TODO: order.setSkillTagId / setTitle / setPointAmount(从货架获取)
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setTimeSlotId(dto.getTimeSlotId());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("创建订单: orderId={}, buyerId={}, shelfId={}", order.getId(), userId, dto.getShelfId());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromDemand(Long userId, CreateOrderFromDemandDto dto) {
        // TODO: 校验需求存在且状态为待接单
        // TODO: 校验货架存在
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId); // 需求发布者为买家
        // TODO: order.setSellerId(接单者=当前用户)
        order.setDemandId(dto.getDemandId());
        order.setSkillShelfId(dto.getShelfId());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        log.info("从需求创建订单: orderId={}, demandId={}", order.getId(), dto.getDemandId());
        return order.getId();
    }

    @Override
    public PageResult<OrderItemVo> listOrders(Long userId, String role, Integer status, int pageNum, int pageSize) {
        // TODO: 按role(buyer/seller)筛选，关联用户表获取昵称
        return PageResult.empty(pageNum, pageSize);
    }

    @Override
    public OrderDetailVo getOrderDetail(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        // TODO: 校验userId是买家或卖家
        // TODO: 转换为VO，关联查询昵称/头像
        OrderDetailVo vo = new OrderDetailVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setPointAmount(order.getPointAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(OrderStatus.ofCode(order.getStatus()).getDesc());
        vo.setVerifyCode(order.getVerifyCode());
        vo.setVerifyCodeExpire(order.getVerifyCodeExpire());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    @Override
    public OrderStatusVo getOrderStatus(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        // TODO: 校验userId是买家或卖家
        OrderStatus status = OrderStatus.ofCode(order.getStatus());
        return new OrderStatusVo(status.getCode(), status.getDesc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVo payOrder(Long userId, Long orderId, String idempotentToken) {
        Order order = getByIdOrThrow(orderId);
        // TODO: 校验userId是买家
        // 幂等校验: 检查Redis中idempotentToken是否已使用
        // 状态校验: 待支付→已支付
        if (!OrderStatus.canTransitTo(order.getStatus(), OrderStatus.PAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "订单状态不允许支付: " + OrderStatus.ofCode(order.getStatus()).getDesc());
        }
        // TODO: 冻结买家积分(调用PointService)
        // TODO: 生成6位核销码，存Redis(30分钟TTL)
        String verifyCode = generateVerifyCode();
        order.setStatus(OrderStatus.PAID.getCode());
        order.setVerifyCode(verifyCode);
        order.setVerifyCodeExpire(LocalDateTime.now().plusMinutes(30));
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        // TODO: 记录幂等Token到Redis
        log.info("订单支付成功: orderId={}, verifyCode={}", orderId, verifyCode);
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
        // TODO: 校验userId是买家或卖家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.CANCELLED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许取消: " + current.getDesc());
        }
        // TODO: 退改扣分逻辑(调用PointService/CreditService)
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
        // TODO: 校验userId是卖家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.IN_SERVICE.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许开始服务: " + current.getDesc());
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
        // TODO: 校验userId是买家
        VerifyCodeVo vo = new VerifyCodeVo();
        vo.setVerifyCode(order.getVerifyCode());
        vo.setExpireTime(order.getVerifyCodeExpire());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyCodeVo refreshVerifyCode(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        // TODO: 校验userId是买家，状态为已支付
        String newCode = generateVerifyCode();
        order.setVerifyCode(newCode);
        order.setVerifyCodeExpire(LocalDateTime.now().plusMinutes(30));
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
        // TODO: 校验userId是卖家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.PENDING_CONFIRM.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许核销: " + current.getDesc());
        }
        // TODO: 校验核销码(Redis)是否匹配且未过期
        order.setStatus(OrderStatus.PENDING_CONFIRM.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        log.info("订单核销: orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long userId, Long orderId) {
        Order order = getByIdOrThrow(orderId);
        // TODO: 校验userId是买家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.COMPLETED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许确认: " + current.getDesc());
        }
        // TODO: 结算积分(解冻→转给卖家，调用PointService)
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
        // TODO: 校验userId是买家或卖家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.DISPUTING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许争议: " + current.getDesc());
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
        // TODO: 根据仲裁结果决定→COMPLETED或REFUNDED
        // TODO: 退款逻辑(调用PointService)
        if (dto.getRefundAmount() != null && dto.getRefundAmount() > 0) {
            order.setStatus(OrderStatus.REFUNDED.getCode());
        } else {
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
        // TODO: 校验userId是买家
        OrderStatus current = OrderStatus.ofCode(order.getStatus());
        if (!OrderStatus.canTransitTo(current.getCode(), OrderStatus.REFUNDED.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许退款: " + current.getDesc());
        }
        // TODO: 退款逻辑(解冻积分返还买家)
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

    private String generateOrderNo() {
        return "DBS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateVerifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
