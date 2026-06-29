package com.dabashou.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 校园认证实体 — 对应 user_campus_auth 表(V1.2.0:12)
 */
@TableName("user_campus_auth")
public class UserCampusAuth {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String authType;
    private String studentNo;
    private String realName;
    private String campus;
    private String college;
    private String idCardHash;
    private Long credentialFileId;
    private Integer status;
    private String reviewRemark;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
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
    public String getIdCardHash() { return idCardHash; }
    public void setIdCardHash(String idCardHash) { this.idCardHash = idCardHash; }
    public Long getCredentialFileId() { return credentialFileId; }
    public void setCredentialFileId(Long credentialFileId) { this.credentialFileId = credentialFileId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
