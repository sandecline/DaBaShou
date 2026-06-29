package com.dabashou.demand.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DemandItemVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long skillTagId;
    private String tagName;
    private String categoryName;
    private String title;
    private String description;
    private Integer pointReward;
    private Integer demandType;
    private Integer locationType;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer status;
    private LocalDateTime deadline;
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public Integer getDemandType() { return demandType; }
    public void setDemandType(Integer demandType) { this.demandType = demandType; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
