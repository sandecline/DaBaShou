package com.dabashou.message.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.message.vo.NotificationVo;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 分页查询当前用户通知
     */
    PageResult<NotificationVo> list(Long userId, String type, Integer isRead, int pageNum, int pageSize);

    /**
     * 未读消息数量
     */
    long unreadCount(Long userId);

    /**
     * 标记单条已读
     */
    void markRead(Long userId, Long id);

    /**
     * 全部标记已读
     */
    void markAllRead(Long userId);

    /**
     * 删除通知
     */
    void delete(Long userId, Long id);

    /**
     * 发送通知（供其他模块调用）
     */
    void send(Long userId, String type, String title, String content, String relatedType, Long relatedId);
}
