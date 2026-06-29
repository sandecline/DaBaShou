package com.dabashou.message.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.message.service.NotificationService;
import com.dabashou.message.vo.NotificationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知", description = "系统通知管理")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表")
    @GetMapping
    public AjaxResult<PageResult<NotificationVo>> getNotifications(
            @Parameter(description = "通知类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(notificationService.getNotifications(SecurityUtil.requireCurrentUserId(), type, isRead, pageNum, pageSize));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread-count")
    public AjaxResult<Integer> getUnreadCount() {
        return AjaxResult.ok(notificationService.getUnreadCount(SecurityUtil.requireCurrentUserId()));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public AjaxResult<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(SecurityUtil.requireCurrentUserId(), id);
        return AjaxResult.ok();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public AjaxResult<Void> markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtil.requireCurrentUserId());
        return AjaxResult.ok();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(SecurityUtil.requireCurrentUserId(), id);
        return AjaxResult.ok();
    }
}
