package com.dabashou.user.api;

import java.math.BigDecimal;

/**
 * 用户模块跨模块API接口
 * 其他模块通过此接口调用用户模块功能
 */
public interface UserApi {

    /**
     * 获取用户昵称
     */
    String getNickname(Long userId);

    /**
     * 获取用户头像
     */
    String getAvatar(Long userId);

    /**
     * 获取用户信任分
     */
    BigDecimal getTrustScore(Long userId);

    /**
     * 增减用户积分余额
     * @param amount 正数增加，负数扣减
     */
    void addPointBalance(Long userId, int amount);

    /**
     * 扣减用户积分余额（余额不足抛异常）
     */
    void deductPointBalance(Long userId, int amount);

    /**
     * 调整用户信任分
     */
    void adjustTrustScore(Long userId, Long orderId, String type, BigDecimal change, String reason);

    /**
     * 检查用户是否存在且正常
     */
    boolean isUserActive(Long userId);
}
