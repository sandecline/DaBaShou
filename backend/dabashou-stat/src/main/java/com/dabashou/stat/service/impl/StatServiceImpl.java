package com.dabashou.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.order.api.OrderApi;
import com.dabashou.stat.domain.DailySummary;
import com.dabashou.stat.domain.SkillHeat;
import com.dabashou.stat.mapper.DailySummaryMapper;
import com.dabashou.stat.mapper.SkillHeatMapper;
import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import com.dabashou.user.api.UserApi;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatServiceImpl implements StatService {

    private final DailySummaryMapper dailySummaryMapper;
    private final SkillHeatMapper skillHeatMapper;
    private final OrderApi orderApi;
    private final UserApi userApi;

    public StatServiceImpl(DailySummaryMapper dailySummaryMapper, SkillHeatMapper skillHeatMapper,
                           OrderApi orderApi, UserApi userApi) {
        this.dailySummaryMapper = dailySummaryMapper;
        this.skillHeatMapper = skillHeatMapper;
        this.orderApi = orderApi;
        this.userApi = userApi;
    }

    @Override
    public PersonalOverviewVo getPersonalOverview(Long userId) {
        PersonalOverviewVo vo = new PersonalOverviewVo();
        vo.setTotalOrders(0);
        vo.setCompletedOrders(0);
        vo.setTotalIncome(0);
        vo.setTotalExpense(0);
        vo.setSkillCount(0);
        vo.setReviewCount(0);
        return vo;
    }

    @Override
    public List<DailyStatVo> getOrderTrend(int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LambdaQueryWrapper<DailySummary> qw = new LambdaQueryWrapper<>();
        qw.ge(DailySummary::getStatDate, from).orderByAsc(DailySummary::getStatDate);
        return dailySummaryMapper.selectList(qw).stream().map(this::toDailyStatVo).collect(Collectors.toList());
    }

    @Override
    public List<DailyStatVo> getPointTrend(Long userId, int days) {
        return getOrderTrend(days);
    }

    @Override
    public List<SkillHeatVo> getSkillHeat(int limit) {
        LambdaQueryWrapper<SkillHeat> qw = new LambdaQueryWrapper<>();
        qw.eq(SkillHeat::getStatDate, LocalDate.now())
          .orderByDesc(SkillHeat::getHeatScore).last("LIMIT " + limit);
        List<SkillHeat> list = skillHeatMapper.selectList(qw);
        if (list.isEmpty()) {
            qw = new LambdaQueryWrapper<>();
            qw.orderByDesc(SkillHeat::getStatDate, SkillHeat::getHeatScore).last("LIMIT " + limit);
            list = skillHeatMapper.selectList(qw);
        }
        return list.stream().map(this::toSkillHeatVo).collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatVo> getCategoryStat() {
        return List.of();
    }

    @Override
    public PlatformOverviewVo getPlatformOverview() {
        PlatformOverviewVo vo = new PlatformOverviewVo();
        vo.setTotalUsers(0);
        vo.setTotalOrders(0);
        vo.setTotalShelves(0);
        vo.setTotalDemands(0);
        vo.setTodayNewUsers(0);
        vo.setTodayNewOrders(0);
        return vo;
    }

    @Override
    public List<DailyStatVo> getDailyTrend(int days) {
        return getOrderTrend(days);
    }

    private DailyStatVo toDailyStatVo(DailySummary s) {
        DailyStatVo vo = new DailyStatVo();
        vo.setStatDate(s.getStatDate());
        vo.setNewUserCount(s.getNewUserCount());
        vo.setActiveUserCount(s.getActiveUserCount());
        vo.setNewOrderCount(s.getNewOrderCount());
        vo.setCompletedOrderCount(s.getCompletedOrderCount());
        vo.setPointInflow(s.getPointInflow());
        vo.setPointOutflow(s.getPointOutflow());
        return vo;
    }

    private SkillHeatVo toSkillHeatVo(SkillHeat h) {
        SkillHeatVo vo = new SkillHeatVo();
        vo.setSkillTagId(h.getSkillTagId());
        vo.setCategoryId(h.getCategoryId());
        vo.setShelfCount(h.getShelfCount());
        vo.setDemandCount(h.getDemandCount());
        vo.setOrderCount(h.getOrderCount());
        vo.setHeatScore(h.getHeatScore());
        return vo;
    }
}
