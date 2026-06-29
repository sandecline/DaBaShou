package com.dabashou.user.api.impl;

import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.user.api.UserApi;
import com.dabashou.user.domain.User;
import com.dabashou.user.service.UserService;
import com.dabashou.user.service.UserTrustScoreLogService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserApiImpl implements UserApi {

    private final UserService userService;
    private final UserTrustScoreLogService trustScoreLogService;

    public UserApiImpl(UserService userService, UserTrustScoreLogService trustScoreLogService) {
        this.userService = userService;
        this.trustScoreLogService = trustScoreLogService;
    }

    @Override
    public String getNickname(Long userId) {
        User user = userService.getById(userId);
        return user != null ? user.getNickname() : null;
    }

    @Override
    public String getAvatar(Long userId) {
        User user = userService.getById(userId);
        return user != null ? user.getAvatar() : null;
    }

    @Override
    public BigDecimal getTrustScore(Long userId) {
        User user = userService.getById(userId);
        return user != null ? user.getTrustScore() : null;
    }

    @Override
    public void addPointBalance(Long userId, int amount) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setPointBalance(user.getPointBalance() + amount);
        userService.updateById(user);
    }

    @Override
    public void deductPointBalance(Long userId, int amount) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (user.getPointBalance() < amount) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分余额不足");
        }
        user.setPointBalance(user.getPointBalance() - amount);
        userService.updateById(user);
    }

    @Override
    public void adjustTrustScore(Long userId, Long orderId, String type, BigDecimal change, String reason) {
        trustScoreLogService.adjustTrustScore(userId, orderId, type, change, reason);
    }

    @Override
    public boolean isUserActive(Long userId) {
        User user = userService.getById(userId);
        return user != null && user.getStatus() == 1;
    }
}
