package com.dabashou.point.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.point.domain.PointFreeze;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分冻结记录Mapper
 */
@Mapper
public interface PointFreezeMapper extends BaseMapper<PointFreeze> {
}
