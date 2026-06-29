package com.dabashou.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 校园认证提交DTO
 */
public class CampusAuthDto {
    @NotBlank private String authType;    // student / teacher
    @NotBlank private String studentNo;   // 学号/工号
    @NotBlank private String realName;    // 真实姓名
    @NotBlank private String campus;      // 校区
    private String college;               // 学院
    private Long credentialFileId;        // 凭证文件ID

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
}
