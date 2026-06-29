package com.dabashou.stat.vo;

public class CategoryStatVo {

    private Long categoryId;
    private String categoryName;
    private Integer count;
    private Double percentage;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}
