package com.dabashou.credit.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.vo.AppealVo;

/**
 * 申诉服务接口
 */
public interface AppealService {

    /**
     * 提交申诉
     */
    Long submit(Long userId, AppealDto dto);

    /**
     * 我的申诉记录（分页）
     */
    PageResult<AppealVo> listMine(Long userId, int pageNum, int pageSize);
}
