package com.dabashou.message.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.message.dto.ChatSessionDto;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 当前用户的会话列表
     */
    PageResult<ChatSessionVo> listSessions(Long userId, int pageNum, int pageSize);

    /**
     * 创建或返回已有会话
     */
    Long createSession(Long userId, ChatSessionDto dto);

    /**
     * 分页查消息
     */
    PageResult<ChatMessageVo> getMessages(Long userId, Long sessionId, int pageNum, int pageSize);

    /**
     * 按对方用户ID查消息（自动查找或创建会话）
     */
    PageResult<ChatMessageVo> getMessagesByTargetUserId(Long userId, Long targetUserId, int pageNum, int pageSize);

    /**
     * 发送消息（REST接口）
     */
    void sendMessageToUser(Long senderId, Long receiverId, String content, Integer msgType);

    /**
     * 发送消息
     */
    void sendMessage(Long senderId, Long sessionId, String content, Integer msgType);
}
