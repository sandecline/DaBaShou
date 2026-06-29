package com.dabashou.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.dabashou.point.vo.SignInVo;
import com.dabashou.point.vo.SignInStatusVo;
import com.dabashou.point.vo.PointStatsVo;
import com.dabashou.point.vo.GuaranteePoolVo;
import com.dabashou.user.api.UserApi;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
        PointBalanceVo vo = new PointBalanceVo();
        int available = userApi.getPointBalance(userId);
        QueryWrapper<PointTransaction> frozenW = new QueryWrapper<>();
        frozenW.eq("user_id", userId).eq("type", 3);
        List<PointTransaction> frozenTrans = baseMapper.selectList(frozenW);
        QueryWrapper<PointTransaction> unfrozenW = new QueryWrapper<>();
        unfrozenW.eq("user_id", userId).eq("type", 4);
        List<PointTransaction> unfrozenTrans = baseMapper.selectList(unfrozenW);
        int frozenSum = frozenTrans.stream().mapToInt(PointTransaction::getAmount).sum();
        int unfrozenSum = unfrozenTrans.stream().mapToInt(PointTransaction::getAmount).sum();
        int frozen = frozenSum - unfrozenSum;
        vo.setAvailable(available);
        vo.setFrozen(frozen);
        vo.setTotal(available + frozen);
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
    public void unfreezeAndTransfer(Long buyerId, Long sellerId, Long orderId, int amount, String description) {
        // 给卖家增加积分
        userApi.addPointBalance(sellerId, amount);
        // 记录买家解冻流水
        recordTransaction(buyerId, orderId, 4, amount, "解冻:" + description);
        // 记录卖家收入流水
        recordTransaction(sellerId, orderId, 1, amount, "结算收入:" + description);
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
    public SignInVo signIn(Long userId) {
        String today = LocalDate.now().toString();
        String key = "dbs:sign:" + userId + ":" + today;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "1", 25, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(set)) {
            throw new BusinessException(ErrorCode.CONFLICT, "今日已签到");
        }
        String streakKey = "dbs:sign:streak:" + userId;
        String streak = redisTemplate.opsForValue().get(streakKey);
        int days = streak != null ? Integer.parseInt(streak) + 1 : 1;
        redisTemplate.opsForValue().set(streakKey, String.valueOf(days), 48, TimeUnit.HOURS);
        int reward = Math.min(days, 7) * 10;
        reward(userId, reward, "每日签到(连续" + days + "天)");
        return new SignInVo(reward, days);
    }

    @Override
    public SignInStatusVo getSignInStatus(Long userId) {
        SignInStatusVo vo = new SignInStatusVo();
        String today = LocalDate.now().toString();
        String key = "dbs:sign:" + userId + ":" + today;
        vo.setTodaySigned(Boolean.TRUE.equals(redisTemplate.hasKey(key)));
        String streakKey = "dbs:sign:streak:" + userId;
        String streak = redisTemplate.opsForValue().get(streakKey);
        int days = streak != null ? Integer.parseInt(streak) : 0;
        vo.setConsecutiveDays(days);
        vo.setReward(Math.min(days, 7) * 10);
        return vo;
    }

    @Override
    public PointStatsVo getStats(Long userId) {
        PointStatsVo vo = new PointStatsVo();
        QueryWrapper<PointTransaction> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        List<PointTransaction> all = baseMapper.selectList(qw);
        vo.setTotalIncome(all.stream().filter(t -> t.getType() == 1).mapToInt(PointTransaction::getAmount).sum());
        vo.setTotalExpense(all.stream().filter(t -> t.getType() == 2).mapToInt(PointTransaction::getAmount).sum());
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        vo.setMonthIncome(all.stream().filter(t -> t.getType() == 1 && t.getCreateTime() != null && t.getCreateTime().toLocalDate().compareTo(monthStart) >= 0).mapToInt(PointTransaction::getAmount).sum());
        vo.setMonthExpense(all.stream().filter(t -> t.getType() == 2 && t.getCreateTime() != null && t.getCreateTime().toLocalDate().compareTo(monthStart) >= 0).mapToInt(PointTransaction::getAmount).sum());
        return vo;
    }

    @Override
    public GuaranteePoolVo getGuaranteePool() {
        GuaranteePoolVo vo = new GuaranteePoolVo();
        QueryWrapper<PointTransaction> frozenW = new QueryWrapper<>();
        frozenW.eq("type", 3);
        int frozenSum = baseMapper.selectList(frozenW).stream().mapToInt(PointTransaction::getAmount).sum();
        QueryWrapper<PointTransaction> unfrozenW = new QueryWrapper<>();
        unfrozenW.eq("type", 4);
        int unfrozenSum = baseMapper.selectList(unfrozenW).stream().mapToInt(PointTransaction::getAmount).sum();
        vo.setFrozenAmount(frozenSum - unfrozenSum);
        QueryWrapper<PointTransaction> allW = new QueryWrapper<>();
        int totalIncome = baseMapper.selectList(allW.eq("type", 1)).stream().mapToInt(PointTransaction::getAmount).sum();
        vo.setTotalPool(totalIncome);
        vo.setAvailableAmount(totalIncome - vo.getFrozenAmount());
        return vo;
    }

    private void recordTransaction(Long userId, Long orderId, int type, int amount, String description) {
        int currentBalance = userApi.getPointBalance(userId);
        int balanceAfter;
        // 收入/解冻/退款/奖励 → 余额增加；支出/冻结/扣除 → 余额减少
        if (type == 1 || type == 4 || type == 5) {
            balanceAfter = currentBalance + amount;
        } else {
            balanceAfter = currentBalance - amount;
        }
        PointTransaction trans = new PointTransaction();
        trans.setUserId(userId);
        trans.setOrderId(orderId);
        trans.setType(type);
        trans.setAmount(amount);
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription(description);
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
