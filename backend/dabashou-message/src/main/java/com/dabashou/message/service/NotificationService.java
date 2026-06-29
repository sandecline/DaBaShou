package com.dabashou.message.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.message.vo.NotificationVo;

public interface NotificationService {

    PageResult<NotificationVo> getNotifications(Long userId, String type, Boolean isRead, int pageNum, int pageSize);

    int getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long userId, Long notificationId);

    void sendNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId);
}
