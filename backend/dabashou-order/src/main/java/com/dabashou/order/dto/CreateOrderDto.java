package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 从货架创建订单
 */
public class CreateOrderDto {

    @NotNull(message = "货架ID不能为空")
    private Long shelfId;
    private Long timeSlotId;
    private String remark;
    @NotBlank(message = "幂等Token不能为空")
    private String idempotentToken;

    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getIdempotentToken() { return idempotentToken; }
    public void setIdempotentToken(String idempotentToken) { this.idempotentToken = idempotentToken; }
}
