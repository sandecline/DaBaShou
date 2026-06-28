package com.dabashou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.order.domain.Order;
import com.dabashou.order.dto.*;
import com.dabashou.order.vo.*;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 从货架创建订单
     */
    Long createOrderFromShelf(Long userId, CreateOrderDto dto);

    /**
     * 从需求创建订单(揭榜接单)
     */
    Long createOrderFromDemand(Long userId, CreateOrderFromDemandDto dto);

    /**
     * 订单列表(买家/卖家视角)
     */
    PageResult<OrderItemVo> listOrders(Long userId, String role, Integer status, int pageNum, int pageSize);

    /**
     * 订单详情
     */
    OrderDetailVo getOrderDetail(Long userId, Long orderId);

    /**
     * 查询订单状态
     */
    OrderStatusVo getOrderStatus(Long userId, Long orderId);

    /**
     * 支付订单(1→2，冻结积分+生成核销码)
     */
    PayResultVo payOrder(Long userId, Long orderId, String idempotentToken);

    /**
     * 取消订单(1→0，退改扣分)
     */
    void cancelOrder(Long userId, Long orderId, CancelDto dto);

    /**
     * 开始服务(2→3)
     */
    void startService(Long userId, Long orderId);

    /**
     * 获取核销码
     */
    VerifyCodeVo getVerifyCode(Long userId, Long orderId);

    /**
     * 刷新核销码
     */
    VerifyCodeVo refreshVerifyCode(Long userId, Long orderId);

    /**
     * 核销订单(3→4)
     */
    void verifyOrder(Long userId, Long orderId, VerifyDto dto);

    /**
     * 买家确认完成(4→5，结算积分)
     */
    void confirmOrder(Long userId, Long orderId);

    /**
     * 发起争议(4→7)
     */
    void disputeOrder(Long userId, Long orderId, DisputeDto dto);

    /**
     * 仲裁订单(7→5或7→6)
     */
    void arbitrateOrder(Long orderId, ArbitrateDto dto);

    /**
     * 申请退款
     */
    void refundOrder(Long userId, Long orderId, RefundDto dto);
}
