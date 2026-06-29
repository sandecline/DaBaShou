package com.dabashou.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.message.domain.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
