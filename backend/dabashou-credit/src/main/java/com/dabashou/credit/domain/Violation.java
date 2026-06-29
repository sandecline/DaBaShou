package com.dabashou.credit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dabashou.common.core.BaseEntity;
import java.math.BigDecimal;

@TableName("credit_violation")
public class Violation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private String type;
    private String description;
    private BigDecimal penaltyScore;
    private Long reporterId;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPenaltyScore() { return penaltyScore; }
    public void setPenaltyScore(BigDecimal penaltyScore) { this.penaltyScore = penaltyScore; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
