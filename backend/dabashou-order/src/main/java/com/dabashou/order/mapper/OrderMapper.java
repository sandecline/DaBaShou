package com.dabashou.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.order.domain.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
