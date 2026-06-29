package com.dabashou.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 短信登录DTO
 */
public class SmsLoginDto {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
