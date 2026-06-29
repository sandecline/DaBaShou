package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 仲裁订单(管理员)
 */
public class ArbitrateDto {

    @NotBlank(message = "仲裁结果不能为空")
    private String result;
    private String reason;
    /** 退款积分(可选，null表示不退款) */
    private Integer refundAmount;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Integer refundAmount) { this.refundAmount = refundAmount; }
}
