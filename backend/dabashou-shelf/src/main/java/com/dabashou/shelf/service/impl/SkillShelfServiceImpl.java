package com.dabashou.shelf.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.core.PageResult;
import com.dabashou.shelf.domain.SkillShelf;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.mapper.SkillShelfMapper;
import com.dabashou.shelf.service.SkillShelfService;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillShelfServiceImpl extends ServiceImpl<SkillShelfMapper, SkillShelf>
        implements SkillShelfService {

    @Override
    @Transactional
    public Long publish(Long userId, SkillShelfDto dto) {
        SkillShelf shelf = new SkillShelf();
        shelf.setUserId(userId);
        shelf.setSkillTagId(dto.getSkillTagId());
        shelf.setTitle(dto.getTitle());
        shelf.setDescription(dto.getDescription());
        shelf.setPointPrice(dto.getPointPrice());
        shelf.setDurationMinutes(dto.getDurationMinutes());
        shelf.setLocationType(dto.getLocationType() != null ? dto.getLocationType() : 1);
        shelf.setStatus(1);
        save(shelf);
        return shelf.getId();
    }

    @Override
    @Transactional
    public void update(Long userId, Long shelfId, SkillShelfDto dto) {
        SkillShelf shelf = getById(shelfId);
        if (shelf == null || !shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        shelf.setSkillTagId(dto.getSkillTagId());
        shelf.setTitle(dto.getTitle());
        shelf.setDescription(dto.getDescription());
        shelf.setPointPrice(dto.getPointPrice());
        shelf.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getLocationType() != null) shelf.setLocationType(dto.getLocationType());
        updateById(shelf);
    }

    @Override
    public ShelfDetailVo getDetail(Long shelfId) {
        SkillShelf shelf = getById(shelfId);
        if (shelf == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        return toDetailVo(shelf);
    }

    @Override
    public PageResult<ShelfItemVo> search(String keyword, Long categoryId, Long tagId,
                                           Integer locationType, String sortBy, int pageNum, int pageSize) {
        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        var wrapper = lambdaQuery()
                .eq(SkillShelf::getStatus, 1)
                .eq(tagId != null, SkillShelf::getSkillTagId, tagId)
                .eq(locationType != null, SkillShelf::getLocationType, locationType)
                .like(keyword != null, SkillShelf::getTitle, keyword);
        if ("price".equals(sortBy)) {
            wrapper.orderByAsc(SkillShelf::getPointPrice);
        } else {
            wrapper.orderByDesc(SkillShelf::getCreateTime);
        }
        var result = page(page, wrapper);
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toItemVo).toList(), pageNum, pageSize);
    }

    @Override
    public PageResult<ShelfItemVo> getMyShelves(Long userId, int pageNum, int pageSize) {
        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        var result = page(page, lambdaQuery()
                .eq(SkillShelf::getUserId, userId)
                .orderByDesc(SkillShelf::getCreateTime));
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toItemVo).toList(), pageNum, pageSize);
    }

    @Override
    public PageResult<ShelfItemVo> getUserShelves(Long userId, int pageNum, int pageSize) {
        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        var result = page(page, lambdaQuery()
                .eq(SkillShelf::getUserId, userId)
                .eq(SkillShelf::getStatus, 1)
                .orderByDesc(SkillShelf::getCreateTime));
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toItemVo).toList(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void onShelf(Long userId, Long shelfId) {
        SkillShelf shelf = getById(shelfId);
        if (shelf == null || !shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        shelf.setStatus(1);
        updateById(shelf);
    }

    @Override
    @Transactional
    public void offShelf(Long userId, Long shelfId) {
        SkillShelf shelf = getById(shelfId);
        if (shelf == null || !shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        shelf.setStatus(0);
        updateById(shelf);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long shelfId) {
        SkillShelf shelf = getById(shelfId);
        if (shelf == null || !shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        removeById(shelfId);
    }

    private ShelfItemVo toItemVo(SkillShelf s) {
        ShelfItemVo vo = new ShelfItemVo();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setSkillTagId(s.getSkillTagId());
        vo.setTitle(s.getTitle());
        vo.setPointPrice(s.getPointPrice());
        vo.setLocationType(s.getLocationType());
        vo.setStatus(s.getStatus());
        vo.setCreateTime(s.getCreateTime());
        return vo;
    }

    private ShelfDetailVo toDetailVo(SkillShelf s) {
        ShelfDetailVo vo = new ShelfDetailVo();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setSkillTagId(s.getSkillTagId());
        vo.setTitle(s.getTitle());
        vo.setDescription(s.getDescription());
        vo.setPointPrice(s.getPointPrice());
        vo.setDurationMinutes(s.getDurationMinutes());
        vo.setLocationType(s.getLocationType());
        vo.setStatus(s.getStatus());
        vo.setCreateTime(s.getCreateTime());
        return vo;
    }
}
