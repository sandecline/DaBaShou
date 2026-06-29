package com.dabashou.demand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.demand.domain.Demand;
import com.dabashou.demand.dto.AcceptDto;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.dto.UpdateDemandDto;
import com.dabashou.demand.vo.AcceptResultVo;
import com.dabashou.demand.vo.DemandDetailVo;
import com.dabashou.demand.vo.DemandItemVo;

/**
 * 需求服务接口
 */
public interface DemandService extends IService<Demand> {

    /**
     * 发布需求
     */
    Long publish(Long userId, DemandDto dto);

    /**
     * 编辑需求
     */
    void update(Long userId, Long demandId, UpdateDemandDto dto);

    /**
     * 关闭需求
     */
    void close(Long userId, Long demandId);

    /**
     * 删除需求
     */
    void delete(Long userId, Long demandId);

    /**
     * 需求详情
     */
    DemandDetailVo getDetail(Long demandId);

    /**
     * 需求看板搜索
     */
    PageResult<DemandItemVo> search(String keyword, Long categoryId, Long skillTagId,
                                    Integer demandType, Integer status, String sortBy,
                                    int pageNum, int pageSize);

    /**
     * 我发布的需求
     */
    PageResult<DemandItemVo> listMine(Long userId, int pageNum, int pageSize);

    /**
     * 揭榜接单 — 返回接单结果信息，前端续调订单模块完成订单创建
     */
    AcceptResultVo accept(Long userId, Long demandId, AcceptDto dto);
}
