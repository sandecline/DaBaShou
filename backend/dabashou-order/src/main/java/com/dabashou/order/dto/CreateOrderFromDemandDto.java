package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 从需求创建订单(揭榜接单)
 */
public class CreateOrderFromDemandDto {

    @NotNull(message = "需求ID不能为空")
    private Long demandId;
    @NotNull(message = "货架ID不能为空")
    private Long shelfId;
    private String remark;
    @NotBlank(message = "幂等Token不能为空")
    private String idempotentToken;

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getIdempotentToken() { return idempotentToken; }
    public void setIdempotentToken(String idempotentToken) { this.idempotentToken = idempotentToken; }
}
