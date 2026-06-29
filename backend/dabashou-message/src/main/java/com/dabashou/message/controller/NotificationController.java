package com.dabashou.message.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.message.service.NotificationService;
import com.dabashou.message.vo.NotificationVo;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 分页查询当前用户通知
     */
    @GetMapping
    public AjaxResult<PageResult<NotificationVo>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            PageResult<NotificationVo> result = notificationService.list(userId, type, isRead, pageNum, pageSize);
            return AjaxResult.ok(result);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 未读通知数量
     */
    @GetMapping("/unread-count")
    public AjaxResult<Long> unreadCount() {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            long count = notificationService.unreadCount(userId);
            return AjaxResult.ok(count);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public AjaxResult<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            notificationService.markRead(userId, id);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public AjaxResult<Void> markAllRead() {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            notificationService.markAllRead(userId);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            notificationService.delete(userId, id);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }
}
