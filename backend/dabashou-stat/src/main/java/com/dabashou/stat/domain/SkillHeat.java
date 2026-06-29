package com.dabashou.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("stat_skill_heat")
public class SkillHeat {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long skillTagId;
    private Long categoryId;
    private Integer shelfCount;
    private Integer demandCount;
    private Integer orderCount;
    private BigDecimal heatScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getShelfCount() { return shelfCount; }
    public void setShelfCount(Integer shelfCount) { this.shelfCount = shelfCount; }
    public Integer getDemandCount() { return demandCount; }
    public void setDemandCount(Integer demandCount) { this.demandCount = demandCount; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public BigDecimal getHeatScore() { return heatScore; }
    public void setHeatScore(BigDecimal heatScore) { this.heatScore = heatScore; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
