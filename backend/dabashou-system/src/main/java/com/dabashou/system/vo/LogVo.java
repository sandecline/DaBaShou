package com.dabashou.system.vo;

import java.time.LocalDateTime;

public class LogVo {

    private Long id;
    private Long operatorId;
    private String operatorNickname;
    private String operationType;
    private String operationContent;
    private String method;
    private String requestUrl;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Integer costTimeMs;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorNickname() { return operatorNickname; }
    public void setOperatorNickname(String operatorNickname) { this.operatorNickname = operatorNickname; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getOperationContent() { return operationContent; }
    public void setOperationContent(String operationContent) { this.operationContent = operationContent; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public Integer getCostTimeMs() { return costTimeMs; }
    public void setCostTimeMs(Integer costTimeMs) { this.costTimeMs = costTimeMs; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
