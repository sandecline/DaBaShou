package com.dabashou.shelf.api;

import java.math.BigDecimal;

/**
 * 货架模块跨模块API接口
 */
public interface ShelfApi {

    /**
     * 获取货架积分价格
     */
    Integer getPointPrice(Long shelfId);

    /**
     * 获取货架技能标签ID
     */
    Long getSkillTagId(Long shelfId);

    /**
     * 获取货架所属用户ID（卖家）
     */
    Long getUserId(Long shelfId);

    /**
     * 获取货架标题
     */
    String getTitle(Long shelfId);

    /**
     * 检查货架是否上架
     */
    boolean isOnShelf(Long shelfId);
}
