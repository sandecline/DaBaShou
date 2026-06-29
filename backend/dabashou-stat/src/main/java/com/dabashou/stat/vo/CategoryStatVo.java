package com.dabashou.stat.vo;

import java.math.BigDecimal;

public class CategoryStatVo {
    private String categoryName;
    private Integer count;
    private BigDecimal percentage;
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
