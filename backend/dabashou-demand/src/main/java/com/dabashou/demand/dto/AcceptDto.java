package com.dabashou.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 接单 DTO
 */
public class AcceptDto {

    @NotNull(message = "货架ID不能为空")
    private Long shelfId;

    @NotBlank(message = "幂等Token不能为空")
    private String idempotentToken;

    private String remark;

    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public String getIdempotentToken() { return idempotentToken; }
    public void setIdempotentToken(String idempotentToken) { this.idempotentToken = idempotentToken; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
