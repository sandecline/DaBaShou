package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发起争议
 */
public class DisputeDto {

    @NotBlank(message = "争议原因不能为空")
    private String reason;
    private String explain;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getExplain() { return explain; }
    public void setExplain(String explain) { this.explain = explain; }
}
