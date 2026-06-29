package com.dabashou.demand.vo;

import java.time.LocalDateTime;

/**
 * 需求列表项 VO
 */
public class DemandItemVo {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String skillTagName;
    private String title;
    private Integer pointReward;
    private LocalDateTime deadline;
    private Integer locationType;
    private Integer demandType;
    private Integer status;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getSkillTagName() { return skillTagName; }
    public void setSkillTagName(String skillTagName) { this.skillTagName = skillTagName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public Integer getDemandType() { return demandType; }
    public void setDemandType(Integer demandType) { this.demandType = demandType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
