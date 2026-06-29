package com.dabashou.stat.vo;

public class PersonalOverviewVo {

    private Integer totalOrders;
    private Integer completedOrders;
    private Integer totalIncome;
    private Integer totalExpense;
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
    public Integer getSkillCount() { return skillCount; }
    public void setSkillCount(Integer skillCount) { this.skillCount = skillCount; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
}
