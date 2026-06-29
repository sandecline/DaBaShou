package com.dabashou.shelf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.shelf.domain.SkillShelf;
import com.dabashou.shelf.domain.TimeSlot;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.mapper.TimeSlotMapper;
import com.dabashou.shelf.service.SkillShelfService;
import com.dabashou.shelf.service.TimeSlotService;
import com.dabashou.shelf.vo.TimeSlotVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeSlotServiceImpl extends ServiceImpl<TimeSlotMapper, TimeSlot>
        implements TimeSlotService {

    private final SkillShelfService shelfService;

    public TimeSlotServiceImpl(SkillShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @Override
    @Transactional
    public void batchSet(Long userId, Long shelfId, List<TimeSlotDto> slots) {
        SkillShelf shelf = shelfService.getById(shelfId);
        if (shelf == null || !shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        List<TimeSlot> entities = slots.stream().map(dto -> {
            TimeSlot slot = new TimeSlot();
            slot.setUserId(userId);
            slot.setDate(dto.getDate());
            slot.setStartTime(dto.getStartTime());
            slot.setEndTime(dto.getEndTime());
            slot.setStatus(1);
            return slot;
        }).toList();
        saveBatch(entities);
    }

    @Override
    public List<TimeSlotVo> getByShelf(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        if (shelf == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        return lambdaQuery()
                .eq(TimeSlot::getUserId, shelf.getUserId())
                .orderByAsc(TimeSlot::getDate)
                .orderByAsc(TimeSlot::getStartTime)
                .list()
                .stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        TimeSlot slot = getById(slotId);
        if (slot == null || !slot.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "时间格子不存在");
        }
        removeById(slotId);
    }

    private TimeSlotVo toVo(TimeSlot t) {
        TimeSlotVo vo = new TimeSlotVo();
        vo.setId(t.getId());
        vo.setDate(t.getDate());
        vo.setStartTime(t.getStartTime());
        vo.setEndTime(t.getEndTime());
        vo.setStatus(t.getStatus());
        return vo;
    }
}
