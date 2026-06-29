package com.dabashou.shelf.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.service.SkillShelfService;
import com.dabashou.shelf.service.TimeSlotService;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;
import com.dabashou.shelf.vo.TimeSlotVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "货架管理", description = "技能服务发布、搜索、时间格子")
@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final SkillShelfService shelfService;
    private final TimeSlotService timeSlotService;

    public ShelfController(SkillShelfService shelfService, TimeSlotService timeSlotService) {
        this.shelfService = shelfService;
        this.timeSlotService = timeSlotService;
    }

    @Operation(summary = "发布服务")
    @PostMapping
    public AjaxResult<Long> publish(@Valid @RequestBody SkillShelfDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(shelfService.publish(userId, dto));
    }

    @Operation(summary = "编辑服务")
    @PutMapping("/{shelfId}")
    public AjaxResult<Void> update(
            @Parameter(description = "货架ID") @PathVariable Long shelfId,
            @Valid @RequestBody SkillShelfDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.update(userId, shelfId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "服务详情")
    @GetMapping("/{shelfId}")
    public AjaxResult<ShelfDetailVo> getDetail(
            @Parameter(description = "货架ID") @PathVariable Long shelfId) {
        return AjaxResult.ok(shelfService.getDetail(shelfId));
    }

    @Operation(summary = "搜索服务")
    @GetMapping
    public AjaxResult<PageResult<ShelfItemVo>> search(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "地点类型") @RequestParam(required = false) Integer locationType,
            @Parameter(description = "排序方式") @RequestParam(required = false, defaultValue = "time") String sortBy,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(shelfService.search(keyword, categoryId, tagId, locationType, sortBy, pageNum, pageSize));
    }

    @Operation(summary = "我的货架")
    @GetMapping("/mine")
    public AjaxResult<PageResult<ShelfItemVo>> getMyShelves(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(shelfService.getMyShelves(userId, pageNum, pageSize));
    }

    @Operation(summary = "上架")
    @PutMapping("/{shelfId}/on")
    public AjaxResult<Void> onShelf(@PathVariable Long shelfId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.onShelf(userId, shelfId);
        return AjaxResult.ok();
    }

    @Operation(summary = "下架")
    @PutMapping("/{shelfId}/off")
    public AjaxResult<Void> offShelf(@PathVariable Long shelfId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.offShelf(userId, shelfId);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除服务")
    @DeleteMapping("/{shelfId}")
    public AjaxResult<Void> delete(@PathVariable Long shelfId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.delete(userId, shelfId);
        return AjaxResult.ok();
    }

    @Operation(summary = "批量设置时间格子")
    @PostMapping("/{shelfId}/timeslots")
    public AjaxResult<Void> setTimeSlots(
            @PathVariable Long shelfId,
            @Valid @RequestBody List<TimeSlotDto> slots) {
        Long userId = SecurityUtil.requireCurrentUserId();
        timeSlotService.batchSet(userId, shelfId, slots);
        return AjaxResult.ok();
    }

    @Operation(summary = "查询时间格子")
    @GetMapping("/{shelfId}/timeslots")
    public AjaxResult<List<TimeSlotVo>> getTimeSlots(@PathVariable Long shelfId) {
        return AjaxResult.ok(timeSlotService.getByShelf(shelfId));
    }

    @Operation(summary = "删除时间格子")
    @DeleteMapping("/{shelfId}/timeslots/{slotId}")
    public AjaxResult<Void> deleteTimeSlot(
            @PathVariable Long shelfId,
            @PathVariable Long slotId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        timeSlotService.deleteSlot(userId, slotId);
        return AjaxResult.ok();
    }
}
