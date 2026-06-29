package com.dabashou.order.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.order.domain.Order;
import com.dabashou.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderApiImpl implements OrderApi {

    private final OrderMapper orderMapper;

    public OrderApiImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public Long getStatus(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? Long.valueOf(o.getStatus()) : null;
    }

    @Override
    public Long getSellerId(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? o.getSellerId() : null;
    }

    @Override
    public Long getBuyerId(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? o.getBuyerId() : null;
    }

    @Override
    public Long getSkillShelfId(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? o.getSkillShelfId() : null;
    }

    @Override
    public Long getPointAmount(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? Long.valueOf(o.getPointAmount()) : null;
    }

    @Override
    public Long getSkillTagId(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? o.getSkillTagId() : null;
    }

    @Override
    public String getTitle(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null ? o.getTitle() : null;
    }
}
