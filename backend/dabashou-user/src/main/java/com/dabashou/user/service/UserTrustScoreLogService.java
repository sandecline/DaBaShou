package com.dabashou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.user.domain.UserTrustScoreLog;
import java.math.BigDecimal;
import java.util.List;

public interface UserTrustScoreLogService extends IService<UserTrustScoreLog> {

    void adjustTrustScore(Long userId, Long orderId, String type, BigDecimal change, String reason);

    List<UserTrustScoreLog> getUserLogs(Long userId, int limit);
}
