package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 核销订单
 */
public class VerifyDto {

    @NotBlank(message = "核销码不能为空")
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
