package com.dabashou.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.message.domain.ChatMessage;
import com.dabashou.message.domain.ChatSession;
import com.dabashou.message.mapper.ChatMessageMapper;
import com.dabashou.message.mapper.ChatSessionMapper;
import com.dabashou.message.service.ChatService;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;
import com.dabashou.user.api.UserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final UserApi userApi;

    public ChatServiceImpl(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper, UserApi userApi) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.userApi = userApi;
    }

    @Override
    @Transactional
    public Long createSession(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能和自己聊天");
        }
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w
            .and(w1 -> w1.eq(ChatSession::getUser1Id, userId).eq(ChatSession::getUser2Id, otherUserId))
            .or(w2 -> w2.eq(ChatSession::getUser1Id, otherUserId).eq(ChatSession::getUser2Id, userId))
        );
        ChatSession existing = sessionMapper.selectOne(qw);
        if (existing != null) return existing.getId();

        ChatSession session = new ChatSession();
        session.setUser1Id(userId);
        session.setUser2Id(otherUserId);
        session.setUnreadCount(0);
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public PageResult<ChatSessionVo> getSessions(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<ChatSession> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatSession::getUser1Id, userId).or().eq(ChatSession::getUser2Id, userId);
        qw.orderByDesc(ChatSession::getLastTime);
        List<ChatSession> list = sessionMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = sessionMapper.selectCount(qw);
        return PageResult.of(total, list.stream().map(s -> toSessionVo(s, userId)).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    public PageResult<ChatMessageVo> getChatHistory(Long userId, Long sessionId, int pageNum, int pageSize) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        if (!userId.equals(session.getUser1Id()) && !userId.equals(session.getUser2Id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问此会话");
        }
        LambdaQueryWrapper<ChatMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatMessage::getSessionId, sessionId).orderByDesc(ChatMessage::getCreateTime);
        List<ChatMessage> list = messageMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = messageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId));
        return PageResult.of(total, list.stream().map(this::toMessageVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void sendMessage(Long senderId, Long sessionId, String content, Integer msgType) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        if (!senderId.equals(session.getUser1Id()) && !senderId.equals(session.getUser2Id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权发送消息");
        }
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setMsgType(msgType != null ? msgType : 1);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        Long receiverId = senderId.equals(session.getUser1Id()) ? session.getUser2Id() : session.getUser1Id();
        LambdaUpdateWrapper<ChatSession> uw = new LambdaUpdateWrapper<>();
        uw.eq(ChatSession::getId, sessionId)
          .set(ChatSession::getLastMessage, content.length() > 100 ? content.substring(0, 100) : content)
          .set(ChatSession::getLastTime, LocalDateTime.now())
          .setSql("unread_count = unread_count + 1");
        sessionMapper.update(null, uw);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long sessionId) {
        LambdaUpdateWrapper<ChatMessage> uw = new LambdaUpdateWrapper<>();
        uw.eq(ChatMessage::getSessionId, sessionId)
          .ne(ChatMessage::getSenderId, userId)
          .eq(ChatMessage::getIsRead, 0)
          .set(ChatMessage::getIsRead, 1);
        messageMapper.update(null, uw);

        LambdaUpdateWrapper<ChatSession> suw = new LambdaUpdateWrapper<>();
        suw.eq(ChatSession::getId, sessionId).set(ChatSession::getUnreadCount, 0);
        sessionMapper.update(null, suw);
    }

    private ChatSessionVo toSessionVo(ChatSession s, Long userId) {
        ChatSessionVo vo = new ChatSessionVo();
        vo.setId(s.getId());
        Long otherUserId = userId.equals(s.getUser1Id()) ? s.getUser2Id() : s.getUser1Id();
        vo.setOtherUserId(otherUserId);
        vo.setOtherUserNickname(userApi.getNickname(otherUserId));
        vo.setOtherUserAvatar(userApi.getAvatar(otherUserId));
        vo.setLastMessage(s.getLastMessage());
        vo.setLastTime(s.getLastTime());
        vo.setUnreadCount(s.getUnreadCount());
        return vo;
    }

    private ChatMessageVo toMessageVo(ChatMessage m) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(m.getId());
        vo.setSessionId(m.getSessionId());
        vo.setSenderId(m.getSenderId());
        vo.setSenderNickname(userApi.getNickname(m.getSenderId()));
        vo.setSenderAvatar(userApi.getAvatar(m.getSenderId()));
        vo.setContent(m.getContent());
        vo.setMsgType(m.getMsgType());
        vo.setIsRead(m.getIsRead() == 1);
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }
}
