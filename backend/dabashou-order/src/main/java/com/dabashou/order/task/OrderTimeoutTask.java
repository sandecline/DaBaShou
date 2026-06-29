package com.dabashou.order.task;

import com.dabashou.common.enums.OrderStatus;
import com.dabashou.order.domain.Order;
import com.dabashou.order.service.OrderService;
import com.dabashou.point.api.PointApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时熔断定时任务
 * - 待支付超15分钟 → 自动取消
 * - 待确认超7天 → 自动完成(结算给卖家)
 */
@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;
    private final PointApi pointApi;

    public OrderTimeoutTask(OrderService orderService, PointApi pointApi) {
        this.orderService = orderService;
        this.pointApi = pointApi;
    }

    /**
     * 每分钟扫描：待支付超15分钟自动取消
     */
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredPendingPayment() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Order> expired = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.getCode())
                .lt(Order::getCreateTime, threshold)
                .list();
        for (Order order : expired) {
            try {
                order.setStatus(OrderStatus.CANCELLED.getCode());
                order.setCancelReason("超时未支付，系统自动取消");
                order.setCancelTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                orderService.updateById(order);
                log.info("超时自动取消订单: orderId={}", order.getId());
            } catch (Exception e) {
                log.error("超时取消订单失败: orderId={}", order.getId(), e);
            }
        }
    }

    /**
     * 每小时扫描：待确认超7天自动完成
     */
    @Scheduled(fixedRate = 3600000)
    public void autoCompletePendingConfirm() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Order> expired = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.PENDING_CONFIRM.getCode())
                .lt(Order::getUpdateTime, threshold)
                .list();
        for (Order order : expired) {
            try {
                pointApi.unfreezeAndTransfer(order.getBuyerId(), order.getSellerId(), order.getId(),
                        order.getPointAmount(), "超时自动确认结算: " + order.getOrderNo());
                order.setStatus(OrderStatus.COMPLETED.getCode());
                order.setCompleteTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                orderService.updateById(order);
                log.info("超时自动确认订单: orderId={}", order.getId());
            } catch (Exception e) {
                log.error("超时确认订单失败: orderId={}", order.getId(), e);
            }
        }
    }
}
