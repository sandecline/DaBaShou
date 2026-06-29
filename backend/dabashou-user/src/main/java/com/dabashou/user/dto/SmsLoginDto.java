package com.dabashou.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 短信验证码登录请求DTO
 */
public class SmsLoginDto {
    @NotBlank private String phone;
    @NotBlank @Size(min = 6, max = 6) private String code;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
