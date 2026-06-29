package com.dabashou.point.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.PointTransType;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.point.domain.PointAccount;
import com.dabashou.point.domain.PointTransaction;
import com.dabashou.point.mapper.PointAccountMapper;
import com.dabashou.point.mapper.PointTransactionMapper;
import com.dabashou.point.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 积分控制器 — 积分余额、流水、统计、签到、担保池
 */
@Tag(name = "积分管理", description = "积分余额查询、流水记录、统计、签到")
@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    private final PointAccountMapper pointAccountMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final JdbcTemplate jdbcTemplate;

    public PointController(PointAccountMapper pointAccountMapper,
                           PointTransactionMapper pointTransactionMapper,
                           JdbcTemplate jdbcTemplate) {
        this.pointAccountMapper = pointAccountMapper;
        this.pointTransactionMapper = pointTransactionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "查询积分余额")
    @GetMapping("/balance")
    public AjaxResult<PointBalanceVo> getBalance() {
        Long userId = SecurityUtil.requireCurrentUserId();
        PointAccount account = pointAccountMapper.selectOne(
                new LambdaQueryWrapper<PointAccount>().eq(PointAccount::getUserId, userId));
        if (account == null) {
            return AjaxResult.ok(new PointBalanceVo(0, 0));
        }
        PointBalanceVo vo = new PointBalanceVo(
                account.getAvailable() != null ? account.getAvailable() : 0,
                account.getFrozen() != null ? account.getFrozen() : 0);
        return AjaxResult.ok(vo);
    }

    @Operation(summary = "积分流水列表")
    @GetMapping("/transactions")
    public AjaxResult<PageResult<PointTransVo>> listTransactions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "流水类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "开始日期(yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "结束日期(yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        Long userId = SecurityUtil.requireCurrentUserId();
        Page<PointTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointTransaction> wrapper = new LambdaQueryWrapper<PointTransaction>()
                .eq(PointTransaction::getUserId, userId)
                .eq(type != null, PointTransaction::getType, type)
                .eq(orderId != null, PointTransaction::getOrderId, orderId)
                .ge(startDate != null, PointTransaction::getCreateTime, startDate)
                .le(endDate != null, PointTransaction::getCreateTime, endDate)
                .orderByDesc(PointTransaction::getCreateTime);
        Page<PointTransaction> result = pointTransactionMapper.selectPage(page, wrapper);

        List<PointTransVo> voList = result.getRecords().stream().map(t -> {
            PointTransVo vo = new PointTransVo();
            vo.setId(t.getId());
            vo.setType(t.getType());
            vo.setTypeDesc(getTypeDesc(t.getType()));
            vo.setAmount(t.getAmount());
            vo.setBalanceAfter(t.getBalanceAfter());
            vo.setOrderId(t.getOrderId());
            vo.setDescription(t.getDescription());
            vo.setCreateTime(t.getCreateTime());
            return vo;
        }).toList();

        PageResult<PointTransVo> pageResult = PageResult.of(
                result.getTotal(), voList, pageNum, pageSize);
        return AjaxResult.ok(pageResult);
    }

    @Operation(summary = "积分流水详情")
    @GetMapping("/transactions/{id}")
    public AjaxResult<PointTransVo> getTransactionDetail(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        PointTransaction t = pointTransactionMapper.selectOne(
                new LambdaQueryWrapper<PointTransaction>()
                        .eq(PointTransaction::getId, id)
                        .eq(PointTransaction::getUserId, userId));
        if (t == null) {
            return AjaxResult.fail(404, "流水记录不存在");
        }

        PointTransVo vo = new PointTransVo();
        vo.setId(t.getId());
        vo.setType(t.getType());
        vo.setTypeDesc(getTypeDesc(t.getType()));
        vo.setAmount(t.getAmount());
        vo.setBalanceAfter(t.getBalanceAfter());
        vo.setOrderId(t.getOrderId());
        vo.setDescription(t.getDescription());
        vo.setCreateTime(t.getCreateTime());
        return AjaxResult.ok(vo);
    }

    @Operation(summary = "积分统计")
    @GetMapping("/stats")
    public AjaxResult<PointStatsVo> getStats() {
        Long userId = SecurityUtil.requireCurrentUserId();

        // 当月起始时间
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN type IN (1,5) THEN amount ELSE 0 END), 0) AS totalIncome,
                    COALESCE(SUM(CASE WHEN type IN (2,3,6) THEN amount ELSE 0 END), 0) AS totalExpense,
                    COALESCE(SUM(CASE WHEN type IN (1,5) AND create_time >= ? THEN amount ELSE 0 END), 0) AS monthIncome,
                    COALESCE(SUM(CASE WHEN type IN (2,3,6) AND create_time >= ? THEN amount ELSE 0 END), 0) AS monthExpense
                FROM dbs_point_transaction WHERE user_id = ?
                """;
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, monthStart, monthStart, userId);

        PointStatsVo vo = new PointStatsVo();
        vo.setTotalIncome(((Number) row.get("totalIncome")).intValue());
        vo.setTotalExpense(((Number) row.get("totalExpense")).intValue());
        vo.setMonthIncome(((Number) row.get("monthIncome")).intValue());
        vo.setMonthExpense(((Number) row.get("monthExpense")).intValue());
        return AjaxResult.ok(vo);
    }

    @Operation(summary = "签到")
    @PostMapping("/sign-in")
    public AjaxResult<SignInVo> signIn() {
        SecurityUtil.requireCurrentUserId();
        // Stub: 签到功能待实现
        return AjaxResult.ok(new SignInVo(5, 1));
    }

    @Operation(summary = "签到状态")
    @GetMapping("/sign-in/status")
    public AjaxResult<SignInVo> getSignInStatus() {
        SecurityUtil.requireCurrentUserId();
        // Stub: 签到功能待实现
        SignInVo vo = new SignInVo();
        vo.setReward(0);
        vo.setConsecutiveDays(0);
        return AjaxResult.ok(vo);
    }

    @Operation(summary = "担保池概览")
    @GetMapping("/guarantee-pool")
    public AjaxResult<GuaranteePoolVo> getGuaranteePool() {
        SecurityUtil.requireCurrentUserId();

        String sql = """
                SELECT
                    COALESCE(SUM(amount), 0) AS totalPool,
                    COALESCE(SUM(CASE WHEN status = 1 THEN amount ELSE 0 END), 0) AS frozenAmount
                FROM dbs_guarantee_pool
                """;
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);

        int totalPool = ((Number) row.get("totalPool")).intValue();
        int frozenAmount = ((Number) row.get("frozenAmount")).intValue();
        int availableAmount = totalPool - frozenAmount;

        return AjaxResult.ok(new GuaranteePoolVo(totalPool, frozenAmount, availableAmount));
    }

    private String getTypeDesc(Integer type) {
        for (PointTransType t : PointTransType.values()) {
            if (t.getCode() == type) {
                return t.getDesc();
            }
        }
        return "未知";
    }
}
