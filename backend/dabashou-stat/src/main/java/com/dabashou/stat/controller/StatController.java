package com.dabashou.stat.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据统计", description = "个人/平台统计")
@RestController
@RequestMapping("/api/v1/stats")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @Operation(summary = "个人概览")
    @GetMapping("/overview")
    public AjaxResult<PersonalOverviewVo> getPersonalOverview() {
        return AjaxResult.ok(statService.getPersonalOverview(SecurityUtil.requireCurrentUserId()));
    }

    @Operation(summary = "订单趋势")
    @GetMapping("/orders/trend")
    public AjaxResult<List<DailyStatVo>> getOrderTrend(@Parameter(description = "天数") @RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(statService.getOrderTrend(days));
    }

    @Operation(summary = "积分趋势")
    @GetMapping("/points/trend")
    public AjaxResult<List<DailyStatVo>> getPointTrend(@Parameter(description = "天数") @RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(statService.getPointTrend(SecurityUtil.requireCurrentUserId(), days));
    }

    @Operation(summary = "技能热度")
    @GetMapping("/skills/heat")
    public AjaxResult<List<SkillHeatVo>> getSkillHeat(@Parameter(description = "数量") @RequestParam(defaultValue = "10") int limit) {
        return AjaxResult.ok(statService.getSkillHeat(limit));
    }

    @Operation(summary = "分类占比")
    @GetMapping("/categories")
    public AjaxResult<List<CategoryStatVo>> getCategoryStat() {
        return AjaxResult.ok(statService.getCategoryStat());
    }
}
