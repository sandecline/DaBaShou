package com.dabashou.demand.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.service.DemandService;
import com.dabashou.demand.vo.DemandItemVo;
import com.dabashou.demand.vo.DemandMatchVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "需求管理", description = "需求发布、搜索、揭榜、匹配")
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
    @PutMapping("/{demandId}")
    public AjaxResult<Void> update(
            @Parameter(description = "需求ID") @PathVariable Long demandId,
            @Valid @RequestBody DemandDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.update(userId, demandId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "需求详情")
    @GetMapping("/{demandId}")
    public AjaxResult<DemandItemVo> getDetail(
            @Parameter(description = "需求ID") @PathVariable Long demandId) {
        return AjaxResult.ok(demandService.getDetail(demandId));
    }

    @Operation(summary = "看板搜索")
    @GetMapping
    public AjaxResult<PageResult<DemandItemVo>> search(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "需求类型") @RequestParam(required = false) Integer demandType,
            @Parameter(description = "排序方式") @RequestParam(required = false, defaultValue = "time") String sortBy,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(demandService.search(keyword, categoryId, tagId, demandType, sortBy, pageNum, pageSize));
    }

    @Operation(summary = "我发布的需求")
    @GetMapping("/mine")
    public AjaxResult<PageResult<DemandItemVo>> getMyDemands(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(demandService.getMyDemands(userId, pageNum, pageSize));
    }

    @Operation(summary = "关闭需求")
    @PutMapping("/{demandId}/close")
    public AjaxResult<Void> close(@PathVariable Long demandId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.close(userId, demandId);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除需求")
    @DeleteMapping("/{demandId}")
    public AjaxResult<Void> delete(@PathVariable Long demandId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.delete(userId, demandId);
        return AjaxResult.ok();
    }

    @Operation(summary = "揭榜接单")
    @PostMapping("/{demandId}/bid")
    public AjaxResult<Void> bid(@PathVariable Long demandId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        demandService.bid(userId, demandId);
        return AjaxResult.ok();
    }

    @Operation(summary = "智能匹配推荐")
    @GetMapping("/{demandId}/match")
    public AjaxResult<List<DemandMatchVo>> match(
            @PathVariable Long demandId,
            @RequestParam(defaultValue = "10") int limit) {
        return AjaxResult.ok(demandService.match(demandId, limit));
    }
}
