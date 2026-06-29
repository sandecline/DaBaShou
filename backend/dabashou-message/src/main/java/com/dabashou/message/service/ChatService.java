package com.dabashou.message.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;

public interface ChatService {

    Long createSession(Long userId, Long otherUserId);

    PageResult<ChatSessionVo> getSessions(Long userId, int pageNum, int pageSize);

    PageResult<ChatMessageVo> getChatHistory(Long userId, Long sessionId, int pageNum, int pageSize);

    void sendMessage(Long senderId, Long sessionId, String content, Integer msgType);

    void markRead(Long userId, Long sessionId);
}
