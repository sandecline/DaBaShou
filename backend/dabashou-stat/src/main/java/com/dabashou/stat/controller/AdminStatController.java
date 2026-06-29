package com.dabashou.stat.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理统计", description = "平台级统计")
@RestController
@RequestMapping("/api/admin/v1/stats")
public class AdminStatController {

    private final StatService statService;

    public AdminStatController(StatService statService) {
        this.statService = statService;
    }

    @Operation(summary = "平台概览")
    @GetMapping("/overview")
    public AjaxResult<PlatformOverviewVo> getPlatformOverview() {
        return AjaxResult.ok(statService.getPlatformOverview());
    }

    @Operation(summary = "每日趋势")
    @GetMapping("/daily-trend")
    public AjaxResult<List<DailyStatVo>> getDailyTrend(@Parameter(description = "天数") @RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(statService.getDailyTrend(days));
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
