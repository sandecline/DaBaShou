package com.dabashou.message.vo;

import java.time.LocalDateTime;

public class ChatSessionVo {

    private Long id;
    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserAvatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherUserNickname() { return otherUserNickname; }
    public void setOtherUserNickname(String otherUserNickname) { this.otherUserNickname = otherUserNickname; }
    public String getOtherUserAvatar() { return otherUserAvatar; }
    public void setOtherUserAvatar(String otherUserAvatar) { this.otherUserAvatar = otherUserAvatar; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getLastTime() { return lastTime; }
    public void setLastTime(LocalDateTime lastTime) { this.lastTime = lastTime; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}
