package com.dabashou.point.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("dbs_point_transaction")
public class PointTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private Integer type;
    private Integer amount;
    private Integer balanceAfter;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
