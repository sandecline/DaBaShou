package com.dabashou.message.vo;

import java.time.LocalDateTime;

public class ChatMessageVo {

    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private Integer msgType;
    private Boolean isRead;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderNickname() { return senderNickname; }
    public void setSenderNickname(String senderNickname) { this.senderNickname = senderNickname; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getMsgType() { return msgType; }
    public void setMsgType(Integer msgType) { this.msgType = msgType; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
