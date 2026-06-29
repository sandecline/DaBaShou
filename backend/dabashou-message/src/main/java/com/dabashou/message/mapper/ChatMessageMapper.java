package com.dabashou.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.message.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
