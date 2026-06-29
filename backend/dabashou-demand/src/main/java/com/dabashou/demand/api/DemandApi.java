package com.dabashou.demand.api;

/**
 * 需求模块跨模块API接口
 */
public interface DemandApi {

    /**
     * 获取需求悬赏积分
     */
    Integer getPointReward(Long demandId);

    /**
     * 获取需求技能标签ID
     */
    Long getSkillTagId(Long demandId);

    /**
     * 获取需求发布者ID
     */
    Long getUserId(Long demandId);

    /**
     * 获取需求标题
     */
    String getTitle(Long demandId);

    /**
     * 获取需求类型(1-求助悬赏 2-技能置换)
     */
    Integer getDemandType(Long demandId);

    /**
     * 更新需求状态
     */
    void updateStatus(Long demandId, int status);
}
