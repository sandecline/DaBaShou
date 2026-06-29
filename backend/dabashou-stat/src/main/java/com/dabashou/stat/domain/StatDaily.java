package com.dabashou.stat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("dbs_stat_daily")
public class StatDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Integer newUserCount;
    private Integer activeUserCount;
    private Integer newOrderCount;
    private Integer completedOrderCount;
    private Integer pointInflow;
    private Integer pointOutflow;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getPointInflow() { return pointInflow; }
    public void setPointInflow(Integer pointInflow) { this.pointInflow = pointInflow; }
    public Integer getPointOutflow() { return pointOutflow; }
    public void setPointOutflow(Integer pointOutflow) { this.pointOutflow = pointOutflow; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
