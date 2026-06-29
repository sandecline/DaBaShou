package com.dabashou.demand.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DemandDto {

    @NotNull(message = "技能标签ID不能为空")
    private Long skillTagId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100")
    private String title;

    private String description;

    @NotNull(message = "悬赏积分不能为空")
    @Min(value = 1, message = "悬赏积分至少1")
    private Integer pointReward;

    private LocalDateTime deadline;

    @Min(value = 1, message = "地点类型1-3")
    @Max(value = 3, message = "地点类型1-3")
    private Integer locationType;

    private BigDecimal longitude;
    private BigDecimal latitude;
    private String campus;
    private String building;

    @Min(value = 1, message = "需求类型1-2")
    @Max(value = 2, message = "需求类型1-2")
    private Integer demandType;

    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public Integer getDemandType() { return demandType; }
    public void setDemandType(Integer demandType) { this.demandType = demandType; }
}
