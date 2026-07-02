package com.dabashou.shelf.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.dto.UpdateShelfDto;
import com.dabashou.shelf.service.ShelfService;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;
import com.dabashou.shelf.vo.TimeSlotVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能货架控制器 — 货架CRUD与时间格子管理
 */
@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    // ===================== 货架 CRUD =====================

    /**
     * 发布货架
     */
    @PostMapping
    public AjaxResult<Long> publish(@Valid @RequestBody SkillShelfDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        Long shelfId = shelfService.publish(userId, dto);
        return AjaxResult.ok("发布成功", shelfId);
    }

    /**
     * 更新货架
     */
    @PutMapping("/{id}")
    public AjaxResult<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateShelfDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.update(userId, id, dto);
        return AjaxResult.ok();
    }

    /**
     * 上架
     */
    @PutMapping("/{id}/on")
    public AjaxResult<Void> on(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.on(userId, id);
        return AjaxResult.ok();
    }

    /**
     * 下架
     */
    @PutMapping("/{id}/off")
    public AjaxResult<Void> off(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.off(userId, id);
        return AjaxResult.ok();
    }

    /**
     * 删除货架
     */
    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.delete(userId, id);
        return AjaxResult.ok();
    }

    /**
     * 获取货架详情
     */
    @GetMapping("/{id}")
    public AjaxResult<ShelfDetailVo> getDetail(@PathVariable Long id) {
        ShelfDetailVo vo = shelfService.getDetail(id);
        return AjaxResult.ok(vo);
    }

    /**
     * 搜索货架
     */
    @GetMapping
    public AjaxResult<PageResult<ShelfItemVo>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long skillTagId,
            @RequestParam(required = false) Integer locationType,
            @RequestParam(defaultValue = "heat") String sortBy,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShelfItemVo> result = shelfService.search(
                keyword, categoryId, skillTagId, locationType, sortBy, pageNum, pageSize);
        return AjaxResult.ok(result);
    }

    /**
     * 我的货架
     */
    @GetMapping("/mine")
    public AjaxResult<PageResult<ShelfItemVo>> listMine(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        PageResult<ShelfItemVo> result = shelfService.listMine(userId, pageNum, pageSize);
        return AjaxResult.ok(result);
    }

    /**
     * 某用户上架的货架
     */
    @GetMapping("/users/{userId}/shelves")
    public AjaxResult<PageResult<ShelfItemVo>> listUserShelves(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShelfItemVo> result = shelfService.listUserShelves(userId, pageNum, pageSize);
        return AjaxResult.ok(result);
    }

    // ===================== 时间格子 =====================

    /**
     * 为货架添加时间格子
     */
    @PostMapping("/{id}/timeslots")
    public AjaxResult<Void> addTimeSlots(@PathVariable Long id,
                                          @Valid @RequestBody List<TimeSlotDto> slots) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.addTimeSlots(userId, id, slots);
        return AjaxResult.ok();
    }

    /**
     * 获取货架的时间格子
     */
    @GetMapping("/{id}/timeslots")
    public AjaxResult<List<TimeSlotVo>> getTimeSlots(@PathVariable Long id) {
        List<TimeSlotVo> slots = shelfService.getTimeSlots(id);
        return AjaxResult.ok(slots);
    }

    /**
     * 删除时间格子
     */
    @DeleteMapping("/{shelfId}/timeslots/{slotId}")
    public AjaxResult<Void> deleteTimeSlot(@PathVariable Long shelfId,
                                            @PathVariable Long slotId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        shelfService.deleteTimeSlot(userId, shelfId, slotId);
        return AjaxResult.ok();
    }
}
