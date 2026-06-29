package com.dabashou.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新位置DTO
 */
public class UpdateLocationDto {

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围-180~180")
    @DecimalMax(value = "180.0", message = "经度范围-180~180")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围-90~90")
    @DecimalMax(value = "90.0", message = "纬度范围-90~90")
    private BigDecimal latitude;

    private String campus;
    private String building;

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
}
