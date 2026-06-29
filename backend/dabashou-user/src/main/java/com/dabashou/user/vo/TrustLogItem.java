package com.dabashou.user.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信任分日志条目
 */
public class TrustLogItem {
    private String type;
    private BigDecimal scoreChange;
    private BigDecimal scoreBefore;
    private BigDecimal scoreAfter;
    private String reason;
    private LocalDateTime createTime;

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
