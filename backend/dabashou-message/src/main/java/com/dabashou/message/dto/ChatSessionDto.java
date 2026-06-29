package com.dabashou.message.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建聊天会话DTO
 */
public class ChatSessionDto {

    @NotNull(message = "对方用户ID不能为空")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
