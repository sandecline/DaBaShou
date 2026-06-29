package com.dabashou.point.api;

/**
 * 积分模块跨模块API接口
 */
public interface PointApi {

    /**
     * 冻结积分（下单时）
     */
    void freeze(Long userId, Long orderId, int amount, String description);

    /**
     * 解冻并转账（确认收货时，积分从买家转给卖家）
     */
    void unfreezeAndTransfer(Long buyerId, Long sellerId, Long orderId, int amount, String description);

    /**
     * 退款解冻（取消订单时）
     */
    void refundFrozen(Long userId, Long orderId, int amount, String description);

    /**
     * 扣减积分
     */
    void deduct(Long userId, int amount, String description);

    /**
     * 奖励积分
     */
    void reward(Long userId, int amount, String description);
}
