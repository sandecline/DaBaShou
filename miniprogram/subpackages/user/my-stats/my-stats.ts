/**
 * 数据统计页
 * 展示用户个人统计数据
 */

import { statService, type OverviewVo } from '../../../services/stat';

interface StatCard {
  label: string;
  value: string;
  unit: string;
  icon: string;
}

Page({
  data: {
    statCards: [] as StatCard[],
    loading: true,
  },

  onLoad() {
    this.loadStats();
  },

  async loadStats() {
    try {
      const res = await statService.getOverview();
      const data = res.data as OverviewVo;
      if (!data) return;

      const praiseRate = data.reviewCount > 0 && data.averageRating
        ? Math.round((data.averageRating / 5) * 100)
        : 0;

      this.setData({
        statCards: [
          { label: '完成订单', value: String(data.completedOrders || 0), unit: '单', icon: 'order' },
          { label: '技能数量', value: String(data.skillCount || 0), unit: '个', icon: 'skill' },
          { label: '获得评价', value: String(data.reviewCount || 0), unit: '条', icon: 'review' },
          { label: '好评率', value: String(praiseRate), unit: '%', icon: 'praise' },
          { label: '积分收入', value: String(data.totalPointsEarned || 0), unit: '积分', icon: 'income' },
          { label: '积分支出', value: String(data.totalPointsSpent || 0), unit: '积分', icon: 'expense' },
        ],
      });
    } catch (err) {
      console.error('[MyStats] 获取统计数据失败:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  onPullDownRefresh() {
    this.loadStats().finally(() => {
      wx.stopPullDownRefresh();
    });
  },
});
