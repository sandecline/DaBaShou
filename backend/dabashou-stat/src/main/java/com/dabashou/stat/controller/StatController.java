package com.dabashou.stat.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "个人统计")
@RestController
@RequestMapping("/api/v1/stats")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) { this.statService = statService; }

    @Operation(summary = "个人概览")
    @GetMapping("/overview")
    public AjaxResult<OverviewVo> overview() {
        return AjaxResult.ok(statService.getOverview(SecurityUtil.requireCurrentUserId()));
    }

    @Operation(summary = "订单趋势")
    @GetMapping("/orders/trend")
    public AjaxResult<List<TrendItemVo>> ordersTrend(@RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(statService.getOrdersTrend(SecurityUtil.requireCurrentUserId(), days));
    }

    @Operation(summary = "积分趋势")
    @GetMapping("/points/trend")
    public AjaxResult<List<TrendItemVo>> pointsTrend(@RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(statService.getPointsTrend(SecurityUtil.requireCurrentUserId(), days));
    }

    @Operation(summary = "技能热度排行")
    @GetMapping("/skills/heat")
    public AjaxResult<List<SkillHeatVo>> skillsHeat(@RequestParam(defaultValue = "10") int limit) {
        return AjaxResult.ok(statService.getSkillsHeat(limit));
    }

    @Operation(summary = "分类占比")
    @GetMapping("/categories")
    public AjaxResult<List<CategoryStatVo>> categories() {
        return AjaxResult.ok(statService.getCategories());
    }
}
