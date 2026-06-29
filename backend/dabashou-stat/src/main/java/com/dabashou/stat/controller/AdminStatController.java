package com.dabashou.stat.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.stat.service.AdminStatService;
import com.dabashou.stat.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员统计")
@RestController
@RequestMapping("/api/admin/v1/stats")
public class AdminStatController {

    private final AdminStatService adminStatService;

    public AdminStatController(AdminStatService adminStatService) { this.adminStatService = adminStatService; }

    @Operation(summary = "平台总览")
    @GetMapping("/overview")
    public AjaxResult<AdminOverviewVo> overview() {
        return AjaxResult.ok(adminStatService.getOverview());
    }

    @Operation(summary = "每日趋势")
    @GetMapping("/daily-trend")
    public AjaxResult<List<DailyTrendVo>> dailyTrend(@RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(adminStatService.getDailyTrend(days));
    }

    @Operation(summary = "用户活跃度")
    @GetMapping("/user-active")
    public AjaxResult<List<TrendItemVo>> userActive(@RequestParam(defaultValue = "30") int days) {
        return AjaxResult.ok(adminStatService.getUserActive(days));
    }

    @Operation(summary = "信任分分布")
    @GetMapping("/trust-distribution")
    public AjaxResult<List<TrustDistributionVo>> trustDistribution() {
        return AjaxResult.ok(adminStatService.getTrustDistribution());
    }

    @Operation(summary = "数据导出")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam String type) {
        byte[] data = adminStatService.exportData(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
