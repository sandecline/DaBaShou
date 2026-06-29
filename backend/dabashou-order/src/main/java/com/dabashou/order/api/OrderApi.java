package com.dabashou.order.api;

public interface OrderApi {

    Long getStatus(Long orderId);

    Long getSellerId(Long orderId);

    Long getBuyerId(Long orderId);

    Long getSkillShelfId(Long orderId);

    Long getPointAmount(Long orderId);

    Long getSkillTagId(Long orderId);

    String getTitle(Long orderId);
}
