package com.dabashou.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.credit.domain.Violation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规举报Mapper
 */
@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {
}
