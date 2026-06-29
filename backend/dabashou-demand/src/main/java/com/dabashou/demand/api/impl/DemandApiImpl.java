package com.dabashou.demand.api.impl;

import com.dabashou.demand.api.DemandApi;
import com.dabashou.demand.domain.Demand;
import com.dabashou.demand.service.DemandService;
import org.springframework.stereotype.Service;

@Service
public class DemandApiImpl implements DemandApi {

    private final DemandService demandService;

    public DemandApiImpl(DemandService demandService) {
        this.demandService = demandService;
    }

    @Override
    public Integer getPointReward(Long demandId) {
        Demand demand = demandService.getById(demandId);
        return demand != null ? demand.getPointReward() : null;
    }

    @Override
    public Long getSkillTagId(Long demandId) {
        Demand demand = demandService.getById(demandId);
        return demand != null ? demand.getSkillTagId() : null;
    }

    @Override
    public Long getUserId(Long demandId) {
        Demand demand = demandService.getById(demandId);
        return demand != null ? demand.getUserId() : null;
    }

    @Override
    public String getTitle(Long demandId) {
        Demand demand = demandService.getById(demandId);
        return demand != null ? demand.getTitle() : null;
    }

    @Override
    public Integer getDemandType(Long demandId) {
        Demand demand = demandService.getById(demandId);
        return demand != null ? demand.getDemandType() : null;
    }

    @Override
    public void updateStatus(Long demandId, int status) {
        Demand demand = demandService.getById(demandId);
        if (demand != null) {
            demand.setStatus(status);
            demandService.updateById(demand);
        }
    }
}
