package com.dabashou.point.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.PointTransType;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.point.domain.GuaranteePool;
import com.dabashou.point.domain.PointAccount;
import com.dabashou.point.domain.PointFreeze;
import com.dabashou.point.domain.PointTransaction;
import com.dabashou.point.mapper.GuaranteePoolMapper;
import com.dabashou.point.mapper.PointAccountMapper;
import com.dabashou.point.mapper.PointFreezeMapper;
import com.dabashou.point.mapper.PointTransactionMapper;
import com.dabashou.point.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 积分服务实现
 */
@Service
public class PointServiceImpl implements PointService {

    private static final Logger log = LoggerFactory.getLogger(PointServiceImpl.class);

    private final PointAccountMapper pointAccountMapper;
    private final PointFreezeMapper pointFreezeMapper;
    private final GuaranteePoolMapper guaranteePoolMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final JdbcTemplate jdbcTemplate;

    public PointServiceImpl(PointAccountMapper pointAccountMapper,
                            PointFreezeMapper pointFreezeMapper,
                            GuaranteePoolMapper guaranteePoolMapper,
                            PointTransactionMapper pointTransactionMapper,
                            JdbcTemplate jdbcTemplate) {
        this.pointAccountMapper = pointAccountMapper;
        this.pointFreezeMapper = pointFreezeMapper;
        this.guaranteePoolMapper = guaranteePoolMapper;
        this.pointTransactionMapper = pointTransactionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long userId, int amount, Long orderId) {
        // 1. 乐观更新：扣减可用积分，增加冻结积分
        String updateAccountSql = "UPDATE dbs_point_account SET available = available - ?, frozen = frozen + ?, update_time = NOW() WHERE user_id = ? AND available >= ?";
        int rows = jdbcTemplate.update(updateAccountSql, amount, amount, userId, amount);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "积分余额不足");
        }

        // 2. 插入冻结记录
        PointFreeze freeze = new PointFreeze();
        freeze.setOrderId(orderId);
        freeze.setUserId(userId);
        freeze.setAmount(amount);
        freeze.setStatus(1);
        freeze.setFreezeTime(LocalDateTime.now());
        pointFreezeMapper.insert(freeze);

        // 3. 查询账户获取 balance_after（只记录可用余额）
        PointAccount account = queryAccount(userId);
        int balanceAfter = account.getAvailable() != null ? account.getAvailable() : 0;

        // 4. 插入流水
        PointTransaction trans = new PointTransaction();
        trans.setUserId(userId);
        trans.setOrderId(orderId);
        trans.setType(PointTransType.FREEZE.getCode());
        trans.setAmount(amount);
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription("订单支付，冻结积分");
        trans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(trans);

        // 5. 插入担保池
        GuaranteePool pool = new GuaranteePool();
        pool.setOrderId(orderId);
        pool.setAmount(amount);
        pool.setStatus(1);
        pool.setCreateTime(LocalDateTime.now());
        guaranteePoolMapper.insert(pool);

        log.info("冻结积分成功: userId={}, amount={}, orderId={}", userId, amount, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long orderId) {
        // 1. 更新冻结状态为已解冻
        String updateFreezeSql = "UPDATE dbs_point_freeze SET status = 2, release_time = NOW() WHERE order_id = ? AND status = 1";
        jdbcTemplate.update(updateFreezeSql, orderId);

        // 2. 查询冻结记录
        PointFreeze freeze = pointFreezeMapper.selectOne(
                new LambdaQueryWrapper<PointFreeze>().eq(PointFreeze::getOrderId, orderId));
        if (freeze == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "冻结记录不存在");
        }

        // 3. 更新账户：解冻
        String updateAccountSql = "UPDATE dbs_point_account SET available = available + ?, frozen = frozen - ?, update_time = NOW() WHERE user_id = ?";
        jdbcTemplate.update(updateAccountSql, freeze.getAmount(), freeze.getAmount(), freeze.getUserId());

        // 4. 查询账户获取 balance_after（只记录可用余额）
        PointAccount account = queryAccount(freeze.getUserId());
        int balanceAfter = account.getAvailable() != null ? account.getAvailable() : 0;

        // 5. 插入流水
        PointTransaction trans = new PointTransaction();
        trans.setUserId(freeze.getUserId());
        trans.setOrderId(orderId);
        trans.setType(PointTransType.UNFREEZE.getCode());
        trans.setAmount(freeze.getAmount());
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription("订单取消，积分解冻");
        trans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(trans);

        // 6. 更新担保池：退还买家
        String updatePoolSql = "UPDATE dbs_guarantee_pool SET status = 3 WHERE order_id = ?";
        jdbcTemplate.update(updatePoolSql, orderId);

        log.info("解冻积分成功: orderId={}, userId={}, amount={}", orderId, freeze.getUserId(), freeze.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long orderId) {
        // 1. 跨模块查询 dbs_order 获取卖家ID和积分金额
        String orderSql = "SELECT buyer_id, seller_id, point_amount FROM dbs_order WHERE id = ?";
        Map<String, Object> orderRow;
        try {
            orderRow = jdbcTemplate.queryForMap(orderSql, orderId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        Long sellerId = ((Number) orderRow.get("seller_id")).longValue();
        Integer pointAmount = ((Number) orderRow.get("point_amount")).intValue();

        // 2. 查询冻结记录
        PointFreeze freeze = pointFreezeMapper.selectOne(
                new LambdaQueryWrapper<PointFreeze>()
                        .eq(PointFreeze::getOrderId, orderId)
                        .eq(PointFreeze::getStatus, 1));
        if (freeze == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "冻结记录不存在或已处理");
        }

        // 3. 更新冻结状态为已结算
        String updateFreezeSql = "UPDATE dbs_point_freeze SET status = 3 WHERE order_id = ? AND status = 1";
        jdbcTemplate.update(updateFreezeSql, orderId);

        // 4. 买家：释放冻结积分（frozen -= amount）
        Long buyerId = ((Number) orderRow.get("buyer_id")).longValue();
        jdbcTemplate.update(
                "UPDATE dbs_point_account SET frozen = frozen - ?, update_time = NOW() WHERE user_id = ?",
                pointAmount, buyerId);

        // 5. 卖家：积分到账
        PointAccount sellerAccount = queryAccount(sellerId);
        if (sellerAccount != null) {
            String updateSellerSql = "UPDATE dbs_point_account SET available = available + ?, total_earned = total_earned + ?, update_time = NOW() WHERE user_id = ?";
            jdbcTemplate.update(updateSellerSql, pointAmount, pointAmount, sellerId);
        } else {
            sellerAccount = new PointAccount();
            sellerAccount.setUserId(sellerId);
            sellerAccount.setAvailable(pointAmount);
            sellerAccount.setFrozen(0);
            sellerAccount.setTotalEarned(pointAmount);
            sellerAccount.setTotalSpent(0);
            sellerAccount.setCreateTime(LocalDateTime.now());
            sellerAccount.setUpdateTime(LocalDateTime.now());
            pointAccountMapper.insert(sellerAccount);
        }

        // 重新查询卖家账户获取 balance_after（只记录可用余额）
        sellerAccount = queryAccount(sellerId);
        int balanceAfter = sellerAccount.getAvailable() != null ? sellerAccount.getAvailable() : 0;

        // 6. 插入卖家收入流水
        PointTransaction trans = new PointTransaction();
        trans.setUserId(sellerId);
        trans.setOrderId(orderId);
        trans.setType(PointTransType.INCOME.getCode());
        trans.setAmount(pointAmount);
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription("订单完成，积分结算");
        trans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(trans);

        // 7. 查询买家账户获取 balance_after，插入买家支出流水
        PointAccount buyerAccount = queryAccount(buyerId);
        int buyerBalanceAfter = buyerAccount.getAvailable() != null ? buyerAccount.getAvailable() : 0;

        PointTransaction buyerTrans = new PointTransaction();
        buyerTrans.setUserId(buyerId);
        buyerTrans.setOrderId(orderId);
        buyerTrans.setType(PointTransType.EXPENSE.getCode());
        buyerTrans.setAmount(pointAmount);
        buyerTrans.setBalanceAfter(buyerBalanceAfter);
        buyerTrans.setDescription("订单完成，积分支出");
        buyerTrans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(buyerTrans);

        // 8. 更新担保池：已结算
        String updatePoolSql = "UPDATE dbs_guarantee_pool SET status = 2, settle_time = NOW() WHERE order_id = ?";
        jdbcTemplate.update(updatePoolSql, orderId);

        log.info("积分结算成功: orderId={}, sellerId={}, amount={}", orderId, sellerId, pointAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long orderId) {
        // 1. 更新冻结状态为已退款
        String updateFreezeSql = "UPDATE dbs_point_freeze SET status = 4, release_time = NOW() WHERE order_id = ? AND status = 1";
        jdbcTemplate.update(updateFreezeSql, orderId);

        // 2. 查询冻结记录
        PointFreeze freeze = pointFreezeMapper.selectOne(
                new LambdaQueryWrapper<PointFreeze>().eq(PointFreeze::getOrderId, orderId));
        if (freeze == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "冻结记录不存在");
        }

        // 3. 更新账户：解冻
        String updateAccountSql = "UPDATE dbs_point_account SET available = available + ?, frozen = frozen - ?, update_time = NOW() WHERE user_id = ?";
        jdbcTemplate.update(updateAccountSql, freeze.getAmount(), freeze.getAmount(), freeze.getUserId());

        // 4. 查询账户获取 balance_after（只记录可用余额）
        PointAccount account = queryAccount(freeze.getUserId());
        int balanceAfter = account.getAvailable() != null ? account.getAvailable() : 0;

        // 5. 插入退款流水
        PointTransaction trans = new PointTransaction();
        trans.setUserId(freeze.getUserId());
        trans.setOrderId(orderId);
        trans.setType(PointTransType.REFUND.getCode());
        trans.setAmount(freeze.getAmount());
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription("订单退款，积分返还");
        trans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(trans);

        // 6. 更新担保池：已退还
        String updatePoolSql = "UPDATE dbs_guarantee_pool SET status = 3 WHERE order_id = ?";
        jdbcTemplate.update(updatePoolSql, orderId);

        log.info("退款成功: orderId={}, userId={}, amount={}", orderId, freeze.getUserId(), freeze.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reward(Long userId, int amount, String reason) {
        // 1. 更新或创建账户
        PointAccount account = queryAccount(userId);
        if (account != null) {
            String updateSql = "UPDATE dbs_point_account SET available = available + ?, total_earned = total_earned + ?, update_time = NOW() WHERE user_id = ?";
            jdbcTemplate.update(updateSql, amount, amount, userId);
        } else {
            account = new PointAccount();
            account.setUserId(userId);
            account.setAvailable(amount);
            account.setFrozen(0);
            account.setTotalEarned(amount);
            account.setTotalSpent(0);
            account.setCreateTime(LocalDateTime.now());
            account.setUpdateTime(LocalDateTime.now());
            pointAccountMapper.insert(account);
        }

        // 2. 查询账户获取 balance_after
        account = queryAccount(userId);
        int balanceAfter = (account.getAvailable() != null ? account.getAvailable() : 0)
                + (account.getFrozen() != null ? account.getFrozen() : 0);

        // 3. 插入流水
        PointTransaction trans = new PointTransaction();
        trans.setUserId(userId);
        trans.setType(PointTransType.SYSTEM_REWARD.getCode());
        trans.setAmount(amount);
        trans.setBalanceAfter(balanceAfter);
        trans.setDescription(reason != null ? reason : "系统奖励");
        trans.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(trans);

        log.info("系统奖励成功: userId={}, amount={}, reason={}", userId, amount, reason);
    }

    private PointAccount queryAccount(Long userId) {
        return pointAccountMapper.selectOne(
                new LambdaQueryWrapper<PointAccount>().eq(PointAccount::getUserId, userId));
    }
}
