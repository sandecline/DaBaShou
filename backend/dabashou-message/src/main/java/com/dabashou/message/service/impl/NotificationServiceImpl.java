package com.dabashou.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dabashou.common.core.PageResult;
import com.dabashou.message.domain.Notification;
import com.dabashou.message.mapper.NotificationMapper;
import com.dabashou.message.service.NotificationService;
import com.dabashou.message.vo.NotificationVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public PageResult<NotificationVo> getNotifications(Long userId, String type, Boolean isRead, int pageNum, int pageSize) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        qw.eq(Notification::getUserId, userId);
        if (type != null) qw.eq(Notification::getType, type);
        if (isRead != null) qw.eq(Notification::getIsRead, isRead ? 1 : 0);
        qw.orderByDesc(Notification::getCreateTime);
        List<Notification> list = notificationMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = notificationMapper.selectCount(qw);
        return PageResult.of(total, list.stream().map(this::toVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        qw.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(qw).intValue();
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        LambdaUpdateWrapper<Notification> uw = new LambdaUpdateWrapper<>();
        uw.eq(Notification::getId, notificationId).eq(Notification::getUserId, userId)
          .set(Notification::getIsRead, 1).set(Notification::getReadTime, LocalDateTime.now());
        notificationMapper.update(null, uw);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> uw = new LambdaUpdateWrapper<>();
        uw.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0)
          .set(Notification::getIsRead, 1).set(Notification::getReadTime, LocalDateTime.now());
        notificationMapper.update(null, uw);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        qw.eq(Notification::getId, notificationId).eq(Notification::getUserId, userId);
        notificationMapper.delete(qw);
    }

    @Override
    public void sendNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedType(relatedType);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    private NotificationVo toVo(Notification n) {
        NotificationVo vo = new NotificationVo();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setRelatedType(n.getRelatedType());
        vo.setRelatedId(n.getRelatedId());
        vo.setIsRead(n.getIsRead() == 1);
        vo.setReadTime(n.getReadTime());
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }
}
