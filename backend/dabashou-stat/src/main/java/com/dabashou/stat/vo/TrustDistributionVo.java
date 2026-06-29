package com.dabashou.stat.vo;

import java.math.BigDecimal;

public class TrustDistributionVo {
    private String level;
    private Integer count;
    private BigDecimal percentage;
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
