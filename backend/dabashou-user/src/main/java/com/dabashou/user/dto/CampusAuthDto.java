package com.dabashou.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 校园认证申请DTO
 */
public class CampusAuthDto {

    @NotBlank(message = "认证类型不能为空")
    private String authType;

    @Size(max = 30, message = "学号最长30")
    private String studentNo;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "姓名最长50")
    private String realName;

    @Size(max = 50, message = "校区最长50")
    private String campus;

    @Size(max = 100, message = "学院最长100")
    private String college;

    private Long credentialFileId;

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
