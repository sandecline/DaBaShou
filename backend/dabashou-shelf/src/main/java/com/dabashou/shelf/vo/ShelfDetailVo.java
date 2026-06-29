package com.dabashou.shelf.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShelfDetailVo {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private BigDecimal userTrustScore;
    private Long skillTagId;
    private String tagName;
    private String categoryName;
    private String title;
    private String description;
    private Integer pointPrice;
    private Integer durationMinutes;
    private Integer locationType;
    private Integer status;
    private List<String> images;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public BigDecimal getUserTrustScore() { return userTrustScore; }
    public void setUserTrustScore(BigDecimal userTrustScore) { this.userTrustScore = userTrustScore; }
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
    public Integer getPointPrice() { return pointPrice; }
    public void setPointPrice(Integer pointPrice) { this.pointPrice = pointPrice; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
