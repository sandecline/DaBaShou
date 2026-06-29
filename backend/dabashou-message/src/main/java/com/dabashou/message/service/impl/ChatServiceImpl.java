package com.dabashou.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.message.domain.ChatMessage;
import com.dabashou.message.domain.ChatSession;
import com.dabashou.message.dto.ChatSessionDto;
import com.dabashou.message.mapper.ChatMessageMapper;
import com.dabashou.message.mapper.ChatSessionMapper;
import com.dabashou.message.service.ChatService;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final JdbcTemplate jdbcTemplate;

    public ChatServiceImpl(ChatSessionMapper chatSessionMapper,
                           ChatMessageMapper chatMessageMapper,
                           JdbcTemplate jdbcTemplate) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResult<ChatSessionVo> listSessions(Long userId, int pageNum, int pageSize) {
        // 分页查会话
        Page<ChatSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatSession::getUser1Id, userId)
                        .or()
                        .eq(ChatSession::getUser2Id, userId))
                .orderByDesc(ChatSession::getLastTime);
        IPage<ChatSession> result = chatSessionMapper.selectPage(page, wrapper);

        List<ChatSessionVo> voList = new ArrayList<>();
        for (ChatSession session : result.getRecords()) {
            Long otherUserId = session.getUser1Id().equals(userId)
                    ? session.getUser2Id()
                    : session.getUser1Id();
            ChatSessionVo vo = new ChatSessionVo();
            vo.setId(session.getId());
            vo.setOtherUserId(otherUserId);
            vo.setLastMessage(session.getLastMessage());
            vo.setLastTime(session.getLastTime());
            vo.setUnreadCount(session.getUnreadCount());

            // 查询对方用户信息
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    "SELECT nickname, avatar FROM dbs_user WHERE id = ?", otherUserId
            );
            if (!users.isEmpty()) {
                Map<String, Object> user = users.get(0);
                vo.setOtherNickname(asString(user.get("nickname")));
                vo.setOtherAvatar(asString(user.get("avatar")));
            }
            voList.add(vo);
        }

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public Long createSession(Long userId, ChatSessionDto dto) {
        Long otherUserId = dto.getUserId();

        // 校验对方用户存在
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id FROM dbs_user WHERE id = ?", otherUserId
        );
        if (users.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "对方用户不存在");
        }

        // 不能和自己创建会话
        if (userId.equals(otherUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能和自己创建会话");
        }

        // 查已有会话（user1 < user2 排序，保持唯一性）
        Long smallerId = userId < otherUserId ? userId : otherUserId;
        Long largerId = userId > otherUserId ? userId : otherUserId;

        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUser1Id, smallerId)
                .eq(ChatSession::getUser2Id, largerId);
        ChatSession existing = chatSessionMapper.selectOne(wrapper);

        if (existing != null) {
            return existing.getId();
        }

        // 创建新会话
        ChatSession session = new ChatSession();
        session.setUser1Id(smallerId);
        session.setUser2Id(largerId);
        session.setUnreadCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);

        log.info("聊天会话创建成功: sessionId={}, user1={}, user2={}", session.getId(), smallerId, largerId);
        return session.getId();
    }

    @Override
    public PageResult<ChatMessageVo> getMessages(Long userId, Long sessionId, int pageNum, int pageSize) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        if (!session.getUser1Id().equals(userId) && !session.getUser2Id().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问此会话");
        }

        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime);
        IPage<ChatMessage> result = chatMessageMapper.selectPage(page, wrapper);

        List<ChatMessageVo> voList = result.getRecords().stream()
                .map(msg -> {
                    ChatMessageVo vo = new ChatMessageVo();
                    vo.setId(msg.getId());
                    vo.setSenderId(msg.getSenderId());
                    vo.setContent(msg.getContent());
                    vo.setMsgType(msg.getMsgType());
                    vo.setIsRead(msg.getIsRead());
                    vo.setCreateTime(msg.getCreateTime());

                    // 查询发送者信息
                    List<Map<String, Object>> users = jdbcTemplate.queryForList(
                            "SELECT nickname, avatar FROM dbs_user WHERE id = ?", msg.getSenderId()
                    );
                    if (!users.isEmpty()) {
                        Map<String, Object> user = users.get(0);
                        vo.setSenderNickname(asString(user.get("nickname")));
                        vo.setSenderAvatar(asString(user.get("avatar")));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public void sendMessage(Long senderId, Long sessionId, String content, Integer msgType) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }

        // 参与权校验：sender 必须是会话成员
        if (!senderId.equals(session.getUser1Id()) && !senderId.equals(session.getUser2Id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权向此会话发送消息");
        }

        // 保存消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : 1);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);

        // 更新会话
        session.setLastMessage(content);
        session.setLastTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);

        // 对方未读计数+1（对方是会话中不是sender的那个）
        Long otherUserId = session.getUser1Id().equals(senderId)
                ? session.getUser2Id()
                : session.getUser1Id();
        jdbcTemplate.update(
                "UPDATE dbs_chat_session SET unread_count = unread_count + 1 WHERE id = ?",
                sessionId
        );

        log.info("聊天消息发送成功: sessionId={}, senderId={}, msgType={}", sessionId, senderId, msgType);
    }

    private String asString(Object value) {
        if (value == null) return null;
        return value.toString();
    }
}
