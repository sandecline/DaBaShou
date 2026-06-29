package com.dabashou.stat.vo;

public class DailyTrendVo {
    private String date;
    private Integer newUserCount;
    private Integer activeUserCount;
    private Integer newOrderCount;
    private Integer completedOrderCount;
    private Integer pointInflow;
    private Integer pointOutflow;
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Integer getNewUserCount() { return newUserCount; }
    public void setNewUserCount(Integer newUserCount) { this.newUserCount = newUserCount; }
    public Integer getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(Integer activeUserCount) { this.activeUserCount = activeUserCount; }
    public Integer getNewOrderCount() { return newOrderCount; }
    public void setNewOrderCount(Integer newOrderCount) { this.newOrderCount = newOrderCount; }
    public Integer getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(Integer completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public Integer getPointInflow() { return pointInflow; }
    public void setPointInflow(Integer pointInflow) { this.pointInflow = pointInflow; }
    public Integer getPointOutflow() { return pointOutflow; }
    public void setPointOutflow(Integer pointOutflow) { this.pointOutflow = pointOutflow; }
}
