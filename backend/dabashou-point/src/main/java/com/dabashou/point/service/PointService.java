package com.dabashou.point.service;

import com.dabashou.point.vo.SignInVo;

/**
 * 积分服务接口 — 由订单模块调用的核心业务
 */
public interface PointService {

    /**
     * 冻结积分（支付时调用）
     * @param userId  买家ID
     * @param amount  冻结金额
     * @param orderId 订单ID
     */
    void freeze(Long userId, int amount, Long orderId);

    /**
     * 解冻积分（订单取消时调用，退还买家）
     * @param orderId 订单ID
     */
    void unfreeze(Long orderId);

    /**
     * 结算积分（订单确认完成时调用，积分转移给卖家）
     * @param orderId 订单ID
     */
    void settle(Long orderId);

    /**
     * 退款（与解冻逻辑相同）
     * @param orderId 订单ID
     */
    void refund(Long orderId);

    /**
     * 系统奖励
     * @param userId 用户ID
     * @param amount 奖励金额
     * @param reason 奖励原因
     */
    void reward(Long userId, int amount, String reason);

    /**
     * 姣忔棩绛惧埌濂栧姳绉垎
     *
     * @param userId 鐢ㄦ埛ID
     * @return 绛惧埌缁撴灉
     */
    SignInVo signIn(Long userId);

    /**
     * 鏌ヨ绛惧埌鐘舵€?
     *
     * @param userId 鐢ㄦ埛ID
     * @return 绛惧埌鐘舵€?
     */
    SignInVo getSignInStatus(Long userId);
}
