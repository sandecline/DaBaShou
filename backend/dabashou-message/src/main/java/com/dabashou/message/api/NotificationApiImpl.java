package com.dabashou.message.api;

import com.dabashou.message.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationApiImpl implements NotificationApi {

    private final NotificationService notificationService;

    public NotificationApiImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void sendNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId) {
        notificationService.sendNotification(userId, type, title, content, relatedType, relatedId);
    }
}
