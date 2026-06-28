package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 取消订单
 */
public class CancelDto {

    @NotBlank(message = "取消原因不能为空")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
