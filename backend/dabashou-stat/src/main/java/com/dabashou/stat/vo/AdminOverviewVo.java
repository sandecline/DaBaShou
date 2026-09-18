package com.dabashou.stat.vo;

import java.math.BigDecimal;

public class AdminOverviewVo {
    private Integer totalUsers;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer totalShelves;
    private Integer totalSkills;
    private Integer totalDemands;
    private Integer todayNewUsers;
    private Integer todayNewOrders;
    private BigDecimal orderCompletionRate;
    private Integer totalPointsInCirculation;
    private Integer pendingAppeals;
    private Integer disputingOrders;
    private Integer pendingCampusAuths;
    private Integer pendingViolations;
    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public Integer getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Integer completedOrders) { this.completedOrders = completedOrders; }
    public Integer getTotalShelves() { return totalShelves; }
    public void setTotalShelves(Integer totalShelves) { this.totalShelves = totalShelves; }
    public Integer getTotalSkills() { return totalSkills; }
    public void setTotalSkills(Integer totalSkills) { this.totalSkills = totalSkills; }
    public Integer getTotalDemands() { return totalDemands; }
    public void setTotalDemands(Integer totalDemands) { this.totalDemands = totalDemands; }
    public Integer getTodayNewUsers() { return todayNewUsers; }
    public void setTodayNewUsers(Integer todayNewUsers) { this.todayNewUsers = todayNewUsers; }
    public Integer getTodayNewOrders() { return todayNewOrders; }
    public void setTodayNewOrders(Integer todayNewOrders) { this.todayNewOrders = todayNewOrders; }
    public BigDecimal getOrderCompletionRate() { return orderCompletionRate; }
    public void setOrderCompletionRate(BigDecimal orderCompletionRate) { this.orderCompletionRate = orderCompletionRate; }
    public Integer getTotalPointsInCirculation() { return totalPointsInCirculation; }
    public void setTotalPointsInCirculation(Integer totalPointsInCirculation) { this.totalPointsInCirculation = totalPointsInCirculation; }
    public Integer getPendingAppeals() { return pendingAppeals; }
    public void setPendingAppeals(Integer pendingAppeals) { this.pendingAppeals = pendingAppeals; }
    public Integer getDisputingOrders() { return disputingOrders; }
    public void setDisputingOrders(Integer disputingOrders) { this.disputingOrders = disputingOrders; }
    public Integer getPendingCampusAuths() { return pendingCampusAuths; }
    public void setPendingCampusAuths(Integer pendingCampusAuths) { this.pendingCampusAuths = pendingCampusAuths; }
    public Integer getPendingViolations() { return pendingViolations; }
    public void setPendingViolations(Integer pendingViolations) { this.pendingViolations = pendingViolations; }
}
