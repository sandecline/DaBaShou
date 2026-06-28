package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 申请退款
 */
public class RefundDto {

    @NotBlank(message = "退款原因不能为空")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
