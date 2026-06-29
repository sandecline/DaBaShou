package com.dabashou.system.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.system.vo.LogVo;

public interface LogService {

    PageResult<LogVo> getLogs(Long operatorId, String operationType, int pageNum, int pageSize);
}
