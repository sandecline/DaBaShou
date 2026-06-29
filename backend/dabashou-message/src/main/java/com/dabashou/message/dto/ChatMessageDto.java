package com.dabashou.message.dto;

import jakarta.validation.constraints.NotNull;

public class ChatMessageDto {

    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotNull(message = "消息内容不能为空")
    private String content;

    private Integer msgType = 1;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getMsgType() { return msgType; }
    public void setMsgType(Integer msgType) { this.msgType = msgType; }
}
