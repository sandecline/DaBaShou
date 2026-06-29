package com.dabashou.stat.service;

import com.dabashou.stat.vo.*;

import java.util.List;

public interface StatService {

    PersonalOverviewVo getPersonalOverview(Long userId);

    List<DailyStatVo> getOrderTrend(int days);

    List<DailyStatVo> getPointTrend(Long userId, int days);

    List<SkillHeatVo> getSkillHeat(int limit);

    List<CategoryStatVo> getCategoryStat();

    PlatformOverviewVo getPlatformOverview();

    List<DailyStatVo> getDailyTrend(int days);
}
