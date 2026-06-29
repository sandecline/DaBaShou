package com.dabashou.point.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.core.PageResult;
import com.dabashou.point.domain.PointTransaction;
import com.dabashou.point.mapper.PointTransactionMapper;
import com.dabashou.point.service.PointService;
import com.dabashou.point.vo.PointBalanceVo;
import com.dabashou.point.vo.PointTransVo;
import com.dabashou.user.api.UserApi;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class PointServiceImpl extends ServiceImpl<PointTransactionMapper, PointTransaction>
        implements PointService {

    private final UserApi userApi;
    private final StringRedisTemplate redisTemplate;

    public PointServiceImpl(UserApi userApi, StringRedisTemplate redisTemplate) {
        this.userApi = userApi;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public PointBalanceVo getBalance(Long userId) {
        // TODO: 计算冻结金额（从订单状态=2的记录汇总）
        PointBalanceVo vo = new PointBalanceVo();
        // 通过userApi获取用户积分余额
        vo.setAvailable(0); // TODO: 从User.pointBalance获取
        vo.setFrozen(0);    // TODO: 计算冻结金额
        vo.setTotal(0);     // TODO: available + frozen
        return vo;
    }

    @Override
    public PageResult<PointTransVo> getTransactions(Long userId, Integer type, Long orderId,
                                                     String startDate, String endDate,
                                                     int pageNum, int pageSize) {
        Page<PointTransaction> page = new Page<>(pageNum, pageSize);
        var wrapper = lambdaQuery()
                .eq(PointTransaction::getUserId, userId)
                .eq(type != null, PointTransaction::getType, type)
                .eq(orderId != null, PointTransaction::getOrderId, orderId)
                .orderByDesc(PointTransaction::getCreateTime);
        var result = page(page, wrapper);
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toVo).toList(), pageNum, pageSize);
    }

    @Override
    public PointTransVo getTransactionDetail(Long userId, Long transId) {
        PointTransaction trans = getById(transId);
        if (trans == null || !trans.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "流水不存在");
        }
        return toVo(trans);
    }

    @Override
    @Transactional
    public void freeze(Long userId, Long orderId, int amount, String description) {
        userApi.deductPointBalance(userId, amount);
        recordTransaction(userId, orderId, 3, amount, description);
    }

    @Override
    @Transactional
    public void unfreezeAndTransfer(Long userId, Long orderId, int amount, String description) {
        // 冻结积分转给卖家
        // TODO: 需要订单信息获取sellerId
        recordTransaction(userId, orderId, 4, amount, "解冻:" + description);
    }

    @Override
    @Transactional
    public void refundFrozen(Long userId, Long orderId, int amount, String description) {
        userApi.addPointBalance(userId, amount);
        recordTransaction(userId, orderId, 4, amount, "退款:" + description);
    }

    @Override
    @Transactional
    public void deduct(Long userId, int amount, String description) {
        userApi.deductPointBalance(userId, amount);
        recordTransaction(userId, null, 2, amount, description);
    }

    @Override
    @Transactional
    public void reward(Long userId, int amount, String description) {
        userApi.addPointBalance(userId, amount);
        recordTransaction(userId, null, 5, amount, description);
    }

    @Override
    @Transactional
    public void signIn(Long userId) {
        String today = LocalDate.now().toString();
        String key = "dbs:sign:" + userId + ":" + today;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "1", 25, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(set)) {
            throw new BusinessException(ErrorCode.CONFLICT, "今日已签到");
        }
        // 连续签到天数
        String streakKey = "dbs:sign:streak:" + userId;
        String streak = redisTemplate.opsForValue().get(streakKey);
        int days = streak != null ? Integer.parseInt(streak) + 1 : 1;
        redisTemplate.opsForValue().set(streakKey, String.valueOf(days), 48, TimeUnit.HOURS);
        // 签到奖励积分
        int reward = Math.min(days, 7) * 10;
        reward(userId, reward, "每日签到(连续" + days + "天)");
    }

    @Override
    public boolean hasSignedToday(Long userId) {
        String today = LocalDate.now().toString();
        String key = "dbs:sign:" + userId + ":" + today;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void recordTransaction(Long userId, Long orderId, int type, int amount, String description) {
        PointTransaction trans = new PointTransaction();
        trans.setUserId(userId);
        trans.setOrderId(orderId);
        trans.setType(type);
        trans.setAmount(amount);
        trans.setDescription(description);
        // TODO: 计算变动后余额
        trans.setBalanceAfter(0);
        save(trans);
    }

    private PointTransVo toVo(PointTransaction t) {
        PointTransVo vo = new PointTransVo();
        vo.setId(t.getId());
        vo.setType(t.getType());
        vo.setTypeName(getTypeName(t.getType()));
        vo.setAmount(t.getAmount());
        vo.setBalanceAfter(t.getBalanceAfter());
        vo.setOrderId(t.getOrderId());
        vo.setDescription(t.getDescription());
        vo.setCreateTime(t.getCreateTime());
        return vo;
    }

    private String getTypeName(int type) {
        return switch (type) {
            case 1 -> "收入";
            case 2 -> "支出";
            case 3 -> "冻结";
            case 4 -> "解冻";
            case 5 -> "系统奖励";
            case 6 -> "系统扣除";
            default -> "未知";
        };
    }
}
