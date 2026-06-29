package com.dabashou.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.order.api.OrderApi;
import com.dabashou.order.domain.Order;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.stat.domain.DailySummary;
import com.dabashou.stat.domain.SkillHeat;
import com.dabashou.stat.mapper.DailySummaryMapper;
import com.dabashou.stat.mapper.SkillHeatMapper;
import com.dabashou.stat.service.StatService;
import com.dabashou.stat.vo.*;
import com.dabashou.user.api.UserApi;
import com.dabashou.user.domain.User;
import com.dabashou.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatServiceImpl implements StatService {

    private final DailySummaryMapper dailySummaryMapper;
    private final SkillHeatMapper skillHeatMapper;
    private final OrderApi orderApi;
    private final UserApi userApi;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public StatServiceImpl(DailySummaryMapper dailySummaryMapper, SkillHeatMapper skillHeatMapper,
                           OrderApi orderApi, UserApi userApi,
                           OrderMapper orderMapper, UserMapper userMapper) {
        this.dailySummaryMapper = dailySummaryMapper;
        this.skillHeatMapper = skillHeatMapper;
        this.orderApi = orderApi;
        this.userApi = userApi;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PersonalOverviewVo getPersonalOverview(Long userId) {
        PersonalOverviewVo vo = new PersonalOverviewVo();
        // 作为买家的订单数
        long buyerOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getBuyerId, userId));
        // 作为卖家的订单数
        long sellerOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getSellerId, userId));
        vo.setTotalOrders((int) (buyerOrders + sellerOrders));
        // 已完成订单数
        long completed = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 5)
                        .and(w -> w.eq(Order::getBuyerId, userId).or().eq(Order::getSellerId, userId)));
        vo.setCompletedOrders((int) completed);
        // 作为卖家的总收入
        List<Order> sellerCompleted = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getSellerId, userId).eq(Order::getStatus, 5));
        int income = sellerCompleted.stream().mapToInt(Order::getPointAmount).sum();
        vo.setTotalIncome(income);
        // 作为买家的总支出
        List<Order> buyerCompleted = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getBuyerId, userId).eq(Order::getStatus, 5));
        int expense = buyerCompleted.stream().mapToInt(Order::getPointAmount).sum();
        vo.setTotalExpense(expense);
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
        // 按skillTagId分组统计订单数
        List<Order> allOrders = orderMapper.selectList(null);
        Map<Long, Long> tagCountMap = allOrders.stream()
                .filter(o -> o.getSkillTagId() != null)
                .collect(Collectors.groupingBy(Order::getSkillTagId, Collectors.counting()));
        long total = tagCountMap.values().stream().mapToLong(Long::longValue).sum();
        List<CategoryStatVo> result = new ArrayList<>();
        tagCountMap.forEach((tagId, count) -> {
            CategoryStatVo vo = new CategoryStatVo();
            vo.setCategoryId(tagId);
            vo.setCount(count.intValue());
            vo.setPercentage(total > 0 ? Math.round(count * 10000.0 / total) / 100.0 : 0);
            result.add(vo);
        });
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        return result;
    }

    @Override
    public PlatformOverviewVo getPlatformOverview() {
        PlatformOverviewVo vo = new PlatformOverviewVo();
        vo.setTotalUsers(Math.toIntExact(userMapper.selectCount(null)));
        vo.setTotalOrders(Math.toIntExact(orderMapper.selectCount(null)));
        vo.setTotalShelves(0);
        vo.setTotalDemands(0);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        vo.setTodayNewUsers(Math.toIntExact(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart))));
        vo.setTodayNewOrders(Math.toIntExact(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart))));
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
