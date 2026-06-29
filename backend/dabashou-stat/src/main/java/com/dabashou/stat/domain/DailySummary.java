package com.dabashou.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("stat_daily_summary")
public class DailySummary {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer newUserCount;
    private Integer activeUserCount;
    private Integer newDemandCount;
    private Integer newOrderCount;
    private Integer completedOrderCount;
    private Integer cancelledOrderCount;
    private Long pointInflow;
    private Long pointOutflow;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Integer getNewUserCount() { return newUserCount; }
    public void setNewUserCount(Integer newUserCount) { this.newUserCount = newUserCount; }
    public Integer getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(Integer activeUserCount) { this.activeUserCount = activeUserCount; }
    public Integer getNewDemandCount() { return newDemandCount; }
    public void setNewDemandCount(Integer newDemandCount) { this.newDemandCount = newDemandCount; }
    public Integer getNewOrderCount() { return newOrderCount; }
    public void setNewOrderCount(Integer newOrderCount) { this.newOrderCount = newOrderCount; }
    public Integer getCompletedOrderCount() { return completedOrderCount; }
    public void setCompletedOrderCount(Integer completedOrderCount) { this.completedOrderCount = completedOrderCount; }
    public Integer getCancelledOrderCount() { return cancelledOrderCount; }
    public void setCancelledOrderCount(Integer cancelledOrderCount) { this.cancelledOrderCount = cancelledOrderCount; }
    public Long getPointInflow() { return pointInflow; }
    public void setPointInflow(Long pointInflow) { this.pointInflow = pointInflow; }
    public Long getPointOutflow() { return pointOutflow; }
    public void setPointOutflow(Long pointOutflow) { this.pointOutflow = pointOutflow; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
