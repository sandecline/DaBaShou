package com.dabashou.shelf.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.dto.UpdateShelfDto;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;
import com.dabashou.shelf.vo.TimeSlotVo;

import java.util.List;

/**
 * 技能货架服务接口
 */
public interface ShelfService {

    /**
     * 发布技能货架
     */
    Long publish(Long userId, SkillShelfDto dto);

    /**
     * 更新货架信息
     */
    void update(Long userId, Long shelfId, UpdateShelfDto dto);

    /**
     * 上架
     */
    void on(Long userId, Long shelfId);

    /**
     * 下架
     */
    void off(Long userId, Long shelfId);

    /**
     * 删除货架（物理删除）
     */
    void delete(Long userId, Long shelfId);

    /**
     * 获取货架详情
     */
    ShelfDetailVo getDetail(Long shelfId);

    /**
     * 搜索货架
     */
    PageResult<ShelfItemVo> search(String keyword, Long categoryId, Long skillTagId,
                                   Integer locationType, String sortBy,
                                   int pageNum, int pageSize);

    /**
     * 我的货架列表
     */
    PageResult<ShelfItemVo> listMine(Long userId, int pageNum, int pageSize);

    /**
     * 某用户上架的货架
     */
    PageResult<ShelfItemVo> listUserShelves(Long userId, int pageNum, int pageSize);

    /**
     * 为货架添加时间格子
     */
    void addTimeSlots(Long userId, Long shelfId, List<TimeSlotDto> slots);

    /**
     * 获取货架所有者的时间格子
     */
    List<TimeSlotVo> getTimeSlots(Long shelfId);

    /**
     * 删除时间格子
     */
    void deleteTimeSlot(Long userId, Long shelfId, Long slotId);
}
