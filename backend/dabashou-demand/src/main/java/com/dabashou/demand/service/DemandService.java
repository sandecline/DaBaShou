package com.dabashou.demand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.demand.domain.Demand;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.vo.DemandItemVo;
import com.dabashou.demand.vo.DemandMatchVo;
import java.util.List;

public interface DemandService extends IService<Demand> {
    Long publish(Long userId, DemandDto dto);
    void update(Long userId, Long demandId, DemandDto dto);
    DemandItemVo getDetail(Long demandId);
    PageResult<DemandItemVo> search(String keyword, Long categoryId, Long tagId, Integer demandType, String sortBy, int pageNum, int pageSize);
    PageResult<DemandItemVo> getMyDemands(Long userId, int pageNum, int pageSize);
    void close(Long userId, Long demandId);
    void delete(Long userId, Long demandId);
    void bid(Long userId, Long demandId);
    List<DemandMatchVo> match(Long demandId, int limit);
}
