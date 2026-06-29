package com.dabashou.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发送短信验证码DTO
 */
public class SmsCodeDto {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
