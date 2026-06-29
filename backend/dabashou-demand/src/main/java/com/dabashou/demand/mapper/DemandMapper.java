package com.dabashou.demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.demand.domain.Demand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 需求Mapper
 */
@Mapper
public interface DemandMapper extends BaseMapper<Demand> {
}
