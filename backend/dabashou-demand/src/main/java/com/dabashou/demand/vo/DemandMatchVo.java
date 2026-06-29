package com.dabashou.demand.vo;

import java.math.BigDecimal;

public class DemandMatchVo {

    private Long shelfId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String title;
    private Integer pointPrice;
    private BigDecimal trustScore;
    private Double matchScore;

    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getPointPrice() { return pointPrice; }
    public void setPointPrice(Integer pointPrice) { this.pointPrice = pointPrice; }
    public BigDecimal getTrustScore() { return trustScore; }
    public void setTrustScore(BigDecimal trustScore) { this.trustScore = trustScore; }
    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
}
