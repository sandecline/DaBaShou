package com.dabashou.message.api;

public interface NotificationApi {

    void sendNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId);
}
