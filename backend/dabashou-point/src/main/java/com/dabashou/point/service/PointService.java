package com.dabashou.point.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.point.domain.PointTransaction;
import com.dabashou.point.vo.PointBalanceVo;
import com.dabashou.point.vo.PointTransVo;

public interface PointService extends IService<PointTransaction> {
    PointBalanceVo getBalance(Long userId);
    PageResult<PointTransVo> getTransactions(Long userId, Integer type, Long orderId, String startDate, String endDate, int pageNum, int pageSize);
    PointTransVo getTransactionDetail(Long userId, Long transId);
    void freeze(Long userId, Long orderId, int amount, String description);
    void unfreezeAndTransfer(Long userId, Long orderId, int amount, String description);
    void refundFrozen(Long userId, Long orderId, int amount, String description);
    void deduct(Long userId, int amount, String description);
    void reward(Long userId, int amount, String description);
    void signIn(Long userId);
    boolean hasSignedToday(Long userId);
}
