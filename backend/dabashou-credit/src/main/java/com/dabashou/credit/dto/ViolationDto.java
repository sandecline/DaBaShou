package com.dabashou.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 举报违规请求
 */
public class ViolationDto {

    @NotNull(message = "被举报用户ID不能为空")
    private Long targetUserId;

    private Long orderId;

    @NotBlank(message = "违规类型不能为空")
    private String type;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private String[] evidence;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String[] getEvidence() { return evidence; }
    public void setEvidence(String[] evidence) { this.evidence = evidence; }
}
