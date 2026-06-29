package com.dabashou.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_log")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operationType;
    private String operationContent;
    private String method;
    private String requestUrl;
    private String requestParams;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Integer costTimeMs;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getOperationContent() { return operationContent; }
    public void setOperationContent(String operationContent) { this.operationContent = operationContent; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
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
