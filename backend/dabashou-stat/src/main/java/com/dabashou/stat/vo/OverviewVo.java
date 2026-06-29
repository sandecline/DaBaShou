package com.dabashou.stat.vo;

import java.math.BigDecimal;

public class OverviewVo {
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer totalIncome;
    private Integer totalExpense;
    private BigDecimal trustScore;
    private Integer skillCount;
    private Integer reviewCount;

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public Integer getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Integer completedOrders) { this.completedOrders = completedOrders; }
    public Integer getTotalIncome() { return totalIncome; }
    public void setTotalIncome(Integer totalIncome) { this.totalIncome = totalIncome; }
    public Integer getTotalExpense() { return totalExpense; }
    public void setTotalExpense(Integer totalExpense) { this.totalExpense = totalExpense; }
    public BigDecimal getTrustScore() { return trustScore; }
    public void setTrustScore(BigDecimal trustScore) { this.trustScore = trustScore; }
    public Integer getSkillCount() { return skillCount; }
    public void setSkillCount(Integer skillCount) { this.skillCount = skillCount; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
}
