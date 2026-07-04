package com.dabashou.credit.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 提交申诉请求
 */
public class AppealDto {

    private Long violationId;

    @NotBlank(message = "申诉理由不能为空")
    private String reason;

    private String[] evidence;

    /** 订单ID — 订单申诉时使用，此时 violationId 可为空 */
    private Long orderId;

    public Long getViolationId() { return violationId; }
    public void setViolationId(Long violationId) { this.violationId = violationId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String[] getEvidence() { return evidence; }
    public void setEvidence(String[] evidence) { this.evidence = evidence; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
