package com.dabashou.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.user.domain.User;
import com.dabashou.user.domain.UserTrustScoreLog;
import com.dabashou.user.mapper.UserTrustScoreLogMapper;
import com.dabashou.user.service.UserService;
import com.dabashou.user.service.UserTrustScoreLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class UserTrustScoreLogServiceImpl extends ServiceImpl<UserTrustScoreLogMapper, UserTrustScoreLog>
        implements UserTrustScoreLogService {

    private final UserService userService;

    public UserTrustScoreLogServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public void adjustTrustScore(Long userId, Long orderId, String type, BigDecimal change, String reason) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        BigDecimal before = user.getTrustScore();
        BigDecimal after = before.add(change).setScale(1, RoundingMode.HALF_UP);
        if (after.compareTo(new BigDecimal("1.0")) < 0) {
            after = new BigDecimal("1.0");
        }
        if (after.compareTo(new BigDecimal("5.0")) > 0) {
            after = new BigDecimal("5.0");
        }
        user.setTrustScore(after);
        userService.updateById(user);

        UserTrustScoreLog log = new UserTrustScoreLog();
        log.setUserId(userId);
        log.setOrderId(orderId);
        log.setType(type);
        log.setScoreChange(change);
        log.setScoreBefore(before);
        log.setScoreAfter(after);
        log.setReason(reason);
        save(log);
    }

    @Override
    public List<UserTrustScoreLog> getUserLogs(Long userId, int limit) {
        return lambdaQuery()
                .eq(UserTrustScoreLog::getUserId, userId)
                .orderByDesc(UserTrustScoreLog::getCreateTime)
                .last("LIMIT " + limit)
                .list();
    }
}
