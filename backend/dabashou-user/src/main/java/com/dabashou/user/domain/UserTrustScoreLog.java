package com.dabashou.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信任分变动记录实体 — 对应 user_trust_score_log 表(V1.2.0:33)
 */
@TableName("user_trust_score_log")
public class UserTrustScoreLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private String type;
    private BigDecimal scoreChange;
    private BigDecimal scoreBefore;
    private BigDecimal scoreAfter;
    private String reason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getScoreChange() { return scoreChange; }
    public void setScoreChange(BigDecimal scoreChange) { this.scoreChange = scoreChange; }
    public BigDecimal getScoreBefore() { return scoreBefore; }
    public void setScoreBefore(BigDecimal scoreBefore) { this.scoreBefore = scoreBefore; }
    public BigDecimal getScoreAfter() { return scoreAfter; }
    public void setScoreAfter(BigDecimal scoreAfter) { this.scoreAfter = scoreAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
