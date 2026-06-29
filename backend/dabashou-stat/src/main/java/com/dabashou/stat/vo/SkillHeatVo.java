package com.dabashou.stat.vo;

import java.math.BigDecimal;

public class SkillHeatVo {
    private Long skillTagId;
    private String skillTagName;
    private Integer shelfCount;
    private Integer demandCount;
    private Integer orderCount;
    private BigDecimal heatScore;
    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getSkillTagName() { return skillTagName; }
    public void setSkillTagName(String skillTagName) { this.skillTagName = skillTagName; }
    public Integer getShelfCount() { return shelfCount; }
    public void setShelfCount(Integer shelfCount) { this.shelfCount = shelfCount; }
    public Integer getDemandCount() { return demandCount; }
    public void setDemandCount(Integer demandCount) { this.demandCount = demandCount; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public BigDecimal getHeatScore() { return heatScore; }
    public void setHeatScore(BigDecimal heatScore) { this.heatScore = heatScore; }
}
