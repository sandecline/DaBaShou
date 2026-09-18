package com.dabashou.order.schedule;

import com.dabashou.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单定时任务
 */
@Component
public class OrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);

    private final OrderService orderService;

    public OrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 每小时检查一次超时的待确认订单，自动完成并结算
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoConfirmTimeoutOrders() {
        try {
            int count = orderService.autoConfirmTimeout();
            if (count > 0) {
                log.info("自动确认超时订单: {} 笔", count);
            }
        } catch (Exception e) {
            log.error("自动确认超时订单任务异常: {}", e.getMessage());
        }
    }
}
