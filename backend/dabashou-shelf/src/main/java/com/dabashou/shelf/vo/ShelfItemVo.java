package com.dabashou.shelf.vo;

import java.time.LocalDateTime;

public class ShelfItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long skillTagId;
    private String tagName;
    private String categoryName;
    private String title;
    private Integer pointPrice;
    private Integer locationType;
    private Integer status;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getPointPrice() { return pointPrice; }
    public void setPointPrice(Integer pointPrice) { this.pointPrice = pointPrice; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
