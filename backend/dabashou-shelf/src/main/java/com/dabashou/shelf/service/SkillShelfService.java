package com.dabashou.shelf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.shelf.domain.SkillShelf;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;

public interface SkillShelfService extends IService<SkillShelf> {
    Long publish(Long userId, SkillShelfDto dto);
    void update(Long userId, Long shelfId, SkillShelfDto dto);
    ShelfDetailVo getDetail(Long shelfId);
    PageResult<ShelfItemVo> search(String keyword, Long categoryId, Long tagId, Integer locationType, String sortBy, int pageNum, int pageSize);
    PageResult<ShelfItemVo> getMyShelves(Long userId, int pageNum, int pageSize);
    PageResult<ShelfItemVo> getUserShelves(Long userId, int pageNum, int pageSize);
    void onShelf(Long userId, Long shelfId);
    void offShelf(Long userId, Long shelfId);
    void delete(Long userId, Long shelfId);
}
