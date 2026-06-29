package com.dabashou.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.common.core.PageResult;
import com.dabashou.system.domain.SysLog;
import com.dabashou.system.mapper.SysLogMapper;
import com.dabashou.system.service.LogService;
import com.dabashou.system.vo.LogVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    private final SysLogMapper logMapper;

    public LogServiceImpl(SysLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public PageResult<LogVo> getLogs(Long operatorId, String operationType, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysLog> qw = new LambdaQueryWrapper<>();
        if (operatorId != null) qw.eq(SysLog::getOperatorId, operatorId);
        if (operationType != null) qw.eq(SysLog::getOperationType, operationType);
        qw.orderByDesc(SysLog::getCreateTime);
        List<SysLog> list = logMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = logMapper.selectCount(qw);
        return PageResult.of(total, list.stream().map(this::toVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    private LogVo toVo(SysLog l) {
        LogVo vo = new LogVo();
        vo.setId(l.getId());
        vo.setOperatorId(l.getOperatorId());
        vo.setOperationType(l.getOperationType());
        vo.setOperationContent(l.getOperationContent());
        vo.setMethod(l.getMethod());
        vo.setRequestUrl(l.getRequestUrl());
        vo.setIp(l.getIp());
        vo.setStatus(l.getStatus());
        vo.setErrorMsg(l.getErrorMsg());
        vo.setCostTimeMs(l.getCostTimeMs());
        vo.setCreateTime(l.getCreateTime());
        return vo;
    }
}
