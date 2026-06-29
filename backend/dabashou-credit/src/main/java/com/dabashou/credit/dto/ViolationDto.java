package com.dabashou.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ViolationDto {

    @NotNull(message = "被举报用户ID不能为空")
    private Long targetUserId;

    private Long orderId;

    @NotBlank(message = "违规类型不能为空")
    private String type;

    @NotBlank(message = "违规描述不能为空")
    private String description;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
