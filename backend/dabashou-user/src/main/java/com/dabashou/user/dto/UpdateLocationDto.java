package com.dabashou.user.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新位置DTO
 */
public class UpdateLocationDto {
    @NotNull private BigDecimal longitude;
    @NotNull private BigDecimal latitude;

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
}
