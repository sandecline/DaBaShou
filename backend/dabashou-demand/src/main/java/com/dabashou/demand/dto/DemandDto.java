package com.dabashou.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 发布需求 DTO
 */
public class DemandDto {

    @NotNull(message = "技能标签ID不能为空")
    private Long skillTagId;

    @NotBlank(message = "需求标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "悬赏积分不能为空")
    private Integer pointReward;

    private String deadline;

    @NotNull(message = "服务方式不能为空")
    private Integer locationType;

    private Integer demandType;

    private BigDecimal longitude;

    private BigDecimal latitude;

    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public Integer getDemandType() { return demandType; }
    public void setDemandType(Integer demandType) { this.demandType = demandType; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
}
