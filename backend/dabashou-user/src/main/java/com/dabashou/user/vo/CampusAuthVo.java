package com.dabashou.user.vo;

import java.time.LocalDateTime;

/**
 * 校园认证VO
 */
public class CampusAuthVo {

    private Long id;
    private String authType;
    private String studentNo;
    private String realName;
    private String campus;
    private String college;
    private Long credentialFileId;
    private Integer status;
    private String statusName;
    private String reviewRemark;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public Long getCredentialFileId() { return credentialFileId; }
    public void setCredentialFileId(Long credentialFileId) { this.credentialFileId = credentialFileId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
