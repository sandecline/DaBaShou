package com.dabashou.demand.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.demand.dto.AcceptDto;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.dto.UpdateDemandDto;
import com.dabashou.demand.service.DemandService;
import com.dabashou.demand.vo.AcceptResultVo;
import com.dabashou.demand.vo.DemandDetailVo;
import com.dabashou.demand.vo.DemandItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 需求控制器
 */
@Tag(name = "需求管理", description = "需求发布、看板、揭榜")
@RestController
@RequestMapping("/api/v1/demands")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @Operation(summary = "发布需求")
    @PostMapping
    public AjaxResult<Long> publish(@Valid @RequestBody DemandDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(demandService.publish(userId, dto));
    }

    @Operation(summary = "编辑需求")
    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateDemandDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.update(userId, id, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "关闭需求")
    @PutMapping("/{id}/close")
    public AjaxResult<Void> close(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.close(userId, id);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除需求")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.delete(userId, id);
        return AjaxResult.ok();
    }

    @Operation(summary = "需求详情")
    @GetMapping("/{id}")
    public AjaxResult<DemandDetailVo> getDetail(@PathVariable Long id) {
        return AjaxResult.ok(demandService.getDetail(id));
    }

    @Operation(summary = "需求看板列表")
    @GetMapping
    public AjaxResult<PageResult<DemandItemVo>> search(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "技能标签ID") @RequestParam(required = false) Long skillTagId,
            @Parameter(description = "需求类型") @RequestParam(required = false) Integer demandType,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序: time/budget/hot") @RequestParam(required = false) String sortBy,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(demandService.search(keyword, categoryId, skillTagId,
                demandType, status, sortBy, pageNum, pageSize));
    }

    @Operation(summary = "我发布的需求")
    @GetMapping("/mine")
    public AjaxResult<PageResult<DemandItemVo>> listMine(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(demandService.listMine(userId, pageNum, pageSize));
    }

    @Operation(summary = "揭榜接单 — 返回接单信息，前端续调 /api/v1/orders/from-demand 完成订单创建")
    @PostMapping("/{id}/accept")
    public AjaxResult<AcceptResultVo> accept(@PathVariable Long id, @Valid @RequestBody AcceptDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(demandService.accept(userId, id, dto));
    }
}
