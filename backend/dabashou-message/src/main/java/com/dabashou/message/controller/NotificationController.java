package com.dabashou.message.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
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
        PageResult<NotificationVo> result = notificationService.list(userId, type, isRead, pageNum, pageSize);
        return AjaxResult.ok(result);
    }

    /**
     * 未读通知数量
     */
    @GetMapping("/unread-count")
    public AjaxResult<Long> unreadCount() {
        Long userId = SecurityUtil.requireCurrentUserId();
        long count = notificationService.unreadCount(userId);
        return AjaxResult.ok(count);
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public AjaxResult<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        notificationService.markRead(userId, id);
        return AjaxResult.ok();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public AjaxResult<Void> markAllRead() {
        Long userId = SecurityUtil.requireCurrentUserId();
        notificationService.markAllRead(userId);
        return AjaxResult.ok();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        notificationService.delete(userId, id);
        return AjaxResult.ok();
    }
}
