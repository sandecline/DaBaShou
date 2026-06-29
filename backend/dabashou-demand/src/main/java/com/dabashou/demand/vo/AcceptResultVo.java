package com.dabashou.demand.vo;

/**
 * 接单结果 — 包含足够信息供前端续调订单创建接口
 */
public class AcceptResultVo {
    private Long demandId;
    private Long shelfId;
    private Long buyerId;   // 需求发布者，即订单买家
    private Long skillTagId;
    private String title;
    private Integer pointReward;
    private String idempotentToken;
    private String remark;

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getPointReward() { return pointReward; }
    public void setPointReward(Integer pointReward) { this.pointReward = pointReward; }
    public String getIdempotentToken() { return idempotentToken; }
    public void setIdempotentToken(String idempotentToken) { this.idempotentToken = idempotentToken; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
