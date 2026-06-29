package com.dabashou.credit.vo;

import java.time.LocalDateTime;

/**
 * 评价视图对象
 */
public class ReviewVo {

    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerNickname;
    private String reviewerAvatar;
    private Long revieweeId;
    private String revieweeNickname;
    private String revieweeAvatar;
    private Integer rating;
    private String content;
    private String images;
    private Integer isAnonymous;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerNickname() { return reviewerNickname; }
    public void setReviewerNickname(String reviewerNickname) { this.reviewerNickname = reviewerNickname; }
    public String getReviewerAvatar() { return reviewerAvatar; }
    public void setReviewerAvatar(String reviewerAvatar) { this.reviewerAvatar = reviewerAvatar; }
    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }
    public String getRevieweeNickname() { return revieweeNickname; }
    public void setRevieweeNickname(String revieweeNickname) { this.revieweeNickname = revieweeNickname; }
    public String getRevieweeAvatar() { return revieweeAvatar; }
    public void setRevieweeAvatar(String revieweeAvatar) { this.revieweeAvatar = revieweeAvatar; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Integer getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
