package com.dabashou.point.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.point.service.PointService;
import com.dabashou.point.vo.PointBalanceVo;
import com.dabashou.point.vo.PointTransVo;
import com.dabashou.point.vo.SignInVo;
import com.dabashou.point.vo.SignInStatusVo;
import com.dabashou.point.vo.PointStatsVo;
import com.dabashou.point.vo.GuaranteePoolVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "积分管理", description = "积分余额、流水、签到")
@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @Operation(summary = "查询积分余额")
    @GetMapping("/balance")
    public AjaxResult<PointBalanceVo> getBalance() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.getBalance(userId));
    }

    @Operation(summary = "积分流水分页")
    @GetMapping("/transactions")
    public AjaxResult<PageResult<PointTransVo>> getTransactions(
            @Parameter(description = "类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.getTransactions(userId, type, orderId, startDate, endDate, pageNum, pageSize));
    }

    @Operation(summary = "流水详情")
    @GetMapping("/transactions/{transId}")
    public AjaxResult<PointTransVo> getTransactionDetail(
            @Parameter(description = "流水ID") @PathVariable Long transId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.getTransactionDetail(userId, transId));
    }

    @Operation(summary = "每日签到")
    @PostMapping("/sign-in")
    public AjaxResult<SignInVo> signIn() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.signIn(userId));
    }

    @Operation(summary = "签到状态")
    @GetMapping("/sign-in/status")
    public AjaxResult<SignInStatusVo> getSignInStatus() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.getSignInStatus(userId));
    }

    @Operation(summary = "收支统计")
    @GetMapping("/stats")
    public AjaxResult<PointStatsVo> getStats() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(pointService.getStats(userId));
    }

    @Operation(summary = "担保池查询")
    @GetMapping("/guarantee-pool")
    public AjaxResult<GuaranteePoolVo> getGuaranteePool() {
        return AjaxResult.ok(pointService.getGuaranteePool());
    }
}
