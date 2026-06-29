package com.dabashou.credit.vo;

import java.time.LocalDateTime;

public class AppealVo {

    private Long id;
    private Long violationId;
    private Long appellantId;
    private String appellantNickname;
    private String reason;
    private Long evidenceFileId;
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getViolationId() { return violationId; }
    public void setViolationId(Long violationId) { this.violationId = violationId; }
    public Long getAppellantId() { return appellantId; }
    public void setAppellantId(Long appellantId) { this.appellantId = appellantId; }
    public String getAppellantNickname() { return appellantNickname; }
    public void setAppellantNickname(String appellantNickname) { this.appellantNickname = appellantNickname; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getEvidenceFileId() { return evidenceFileId; }
    public void setEvidenceFileId(Long evidenceFileId) { this.evidenceFileId = evidenceFileId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
