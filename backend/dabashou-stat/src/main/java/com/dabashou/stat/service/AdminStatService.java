package com.dabashou.stat.service;

import com.dabashou.stat.vo.*;

import java.util.List;

public interface AdminStatService {
    AdminOverviewVo getOverview();
    List<DailyTrendVo> getDailyTrend(int days);
    List<TrendItemVo> getUserActive(int days);
    List<TrustDistributionVo> getTrustDistribution();
    byte[] exportData(String type);
}
