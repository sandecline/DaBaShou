package com.dabashou.demand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.core.PageResult;
import com.dabashou.demand.domain.Demand;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.mapper.DemandMapper;
import com.dabashou.demand.service.DemandService;
import com.dabashou.demand.vo.DemandItemVo;
import com.dabashou.demand.vo.DemandMatchVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand>
        implements DemandService {

    @Override
    @Transactional
    public Long publish(Long userId, DemandDto dto) {
        Demand demand = new Demand();
        demand.setUserId(userId);
        demand.setSkillTagId(dto.getSkillTagId());
        demand.setTitle(dto.getTitle());
        demand.setDescription(dto.getDescription());
        demand.setPointReward(dto.getPointReward());
        demand.setDeadline(dto.getDeadline());
        demand.setLocationType(dto.getLocationType() != null ? dto.getLocationType() : 1);
        demand.setLongitude(dto.getLongitude());
        demand.setLatitude(dto.getLatitude());
        demand.setCampus(dto.getCampus());
        demand.setBuilding(dto.getBuilding());
        demand.setDemandType(dto.getDemandType() != null ? dto.getDemandType() : 1);
        demand.setStatus(1);
        save(demand);
        return demand.getId();
    }

    @Override
    @Transactional
    public void update(Long userId, Long demandId, DemandDto dto) {
        Demand demand = getById(demandId);
        if (demand == null || !demand.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有待接单状态的需求可编辑");
        }
        demand.setSkillTagId(dto.getSkillTagId());
        demand.setTitle(dto.getTitle());
        demand.setDescription(dto.getDescription());
        demand.setPointReward(dto.getPointReward());
        demand.setDeadline(dto.getDeadline());
        if (dto.getLocationType() != null) demand.setLocationType(dto.getLocationType());
        if (dto.getLongitude() != null) demand.setLongitude(dto.getLongitude());
        if (dto.getLatitude() != null) demand.setLatitude(dto.getLatitude());
        if (dto.getCampus() != null) demand.setCampus(dto.getCampus());
        if (dto.getBuilding() != null) demand.setBuilding(dto.getBuilding());
        updateById(demand);
    }

    @Override
    public DemandItemVo getDetail(Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        return toItemVo(demand);
    }

    @Override
    public PageResult<DemandItemVo> search(String keyword, Long categoryId, Long tagId,
                                            Integer demandType, String sortBy, int pageNum, int pageSize) {
        Page<Demand> page = new Page<>(pageNum, pageSize);
        var wrapper = lambdaQuery()
                .eq(Demand::getStatus, 1)
                .eq(tagId != null, Demand::getSkillTagId, tagId)
                .eq(demandType != null, Demand::getDemandType, demandType)
                .like(keyword != null, Demand::getTitle, keyword);
        if ("budget".equals(sortBy)) {
            wrapper.orderByDesc(Demand::getPointReward);
        } else {
            wrapper.orderByDesc(Demand::getCreateTime);
        }
        var result = page(page, wrapper);
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toItemVo).toList(), pageNum, pageSize);
    }

    @Override
    public PageResult<DemandItemVo> getMyDemands(Long userId, int pageNum, int pageSize) {
        Page<Demand> page = new Page<>(pageNum, pageSize);
        var result = page(page, lambdaQuery()
                .eq(Demand::getUserId, userId)
                .orderByDesc(Demand::getCreateTime));
        return PageResult.of(result.getTotal(),
                result.getRecords().stream().map(this:: toItemVo).toList(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void close(Long userId, Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null || !demand.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        demand.setStatus(0);
        updateById(demand);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null || !demand.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        removeById(demandId);
    }

    @Override
    @Transactional
    public void bid(Long userId, Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求不在待接单状态");
        }
        if (demand.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能揭自己的榜");
        }
        // TODO: 创建揭榜记录，通知需求发布者
    }

    @Override
    public List<DemandMatchVo> match(Long demandId, int limit) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        // TODO: 智能匹配 — 按skillTagId查找货架，按距离/信任分/价格综合排序
        return List.of();
    }

    private DemandItemVo toItemVo(Demand d) {
        DemandItemVo vo = new DemandItemVo();
        vo.setId(d.getId());
        vo.setUserId(d.getUserId());
        vo.setSkillTagId(d.getSkillTagId());
        vo.setTitle(d.getTitle());
        vo.setDescription(d.getDescription());
        vo.setPointReward(d.getPointReward());
        vo.setDemandType(d.getDemandType());
        vo.setLocationType(d.getLocationType());
        vo.setLongitude(d.getLongitude());
        vo.setLatitude(d.getLatitude());
        vo.setStatus(d.getStatus());
        vo.setDeadline(d.getDeadline());
        vo.setCreateTime(d.getCreateTime());
        return vo;
    }
}
