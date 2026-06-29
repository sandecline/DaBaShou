package com.dabashou.demand.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求详情 VO
 */
public class DemandDetailVo extends DemandItemVo {

    private String description;
    private String demandTypeDesc;
    private String statusDesc;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String campus;
    private String building;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDemandTypeDesc() { return demandTypeDesc; }
    public void setDemandTypeDesc(String demandTypeDesc) { this.demandTypeDesc = demandTypeDesc; }
    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
}
