package com.dabashou.stat.vo;

import java.time.LocalDate;

public class DailyStatVo {

    private LocalDate statDate;
    private Integer newUserCount;
    private Integer activeUserCount;
    private Integer newOrderCount;
    private Integer completedOrderCount;
    private Long pointInflow;
    private Long pointOutflow;

    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Integer getNewUserCount() { return newUserCount; }
    public void setNewUserCount(Integer newUserCount) { this.newUserCount = newUserCount; }
    public Integer getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(Integer activeUserCount) { this.activeUserCount = activeUserCount; }
    public Integer getNewOrderCount() { return newOrderCount; }
    public void setNewOrderCount(Integer newOrderCount) { this.newOrderCount = newOrderCount; }
    public Integer getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(Integer completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public Long getPointInflow() { return pointInflow; }
    public void setPointInflow(Long pointInflow) { this.pointInflow = pointInflow; }
    public Long getPointOutflow() { return pointOutflow; }
    public void setPointOutflow(Long pointOutflow) { this.pointOutflow = pointOutflow; }
}
