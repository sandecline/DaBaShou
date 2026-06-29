package com.dabashou.credit.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.vo.ViolationVo;

/**
 * 违规举报服务接口
 */
public interface ViolationService {

    /**
     * 举报违规
     */
    Long report(Long userId, ViolationDto dto);

    /**
     * 我被举报的记录（分页）
     */
    PageResult<ViolationVo> listMine(Long userId, int pageNum, int pageSize);
}
