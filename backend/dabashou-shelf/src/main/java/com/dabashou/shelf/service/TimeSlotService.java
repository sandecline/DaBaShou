package com.dabashou.shelf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.shelf.domain.TimeSlot;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.vo.TimeSlotVo;
import java.util.List;

public interface TimeSlotService extends IService<TimeSlot> {
    void batchSet(Long userId, Long shelfId, List<TimeSlotDto> slots);
    List<TimeSlotVo> getByShelf(Long shelfId);
    void deleteSlot(Long userId, Long slotId);
}
