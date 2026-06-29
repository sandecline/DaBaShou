package com.dabashou.stat.service;

import com.dabashou.stat.vo.*;

import java.util.List;

public interface StatService {
    OverviewVo getOverview(Long userId);
    List<TrendItemVo> getOrdersTrend(Long userId, int days);
    List<TrendItemVo> getPointsTrend(Long userId, int days);
    List<SkillHeatVo> getSkillsHeat(int limit);
    List<CategoryStatVo> getCategories();
}
