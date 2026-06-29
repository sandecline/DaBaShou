package com.dabashou.credit.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 违规举报视图对象
 */
public class ViolationVo {

    private Long id;
    private Long userId;
    private Long orderId;
    private String type;
    private String description;
    private BigDecimal penaltyScore;
    private Long reporterId;
    private String reporterNickname;
    private Integer status;
    private LocalDateTime createTime;

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
    public String getReporterNickname() { return reporterNickname; }
    public void setReporterNickname(String reporterNickname) { this.reporterNickname = reporterNickname; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
