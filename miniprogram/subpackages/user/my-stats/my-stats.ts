/**
 * 数据统计页
 * 展示用户个人统计数据
 */

import { userService } from '../../../services/user';
import type { UserProfile } from '../../../types/user';

interface StatCard {
  label: string;
  value: number;
  unit: string;
  icon: string;
}

Page({
  data: {
    /** 统计卡片列表 */
    statCards: [] as StatCard[],
    /** 加载状态 */
    loading: true,
  },

  onLoad() {
    this.loadStats();
  },

  /** 加载用户统计数据 */
  async loadStats() {
    try {
      const res = await userService.getProfile();
      const data = (res.data || res) as UserProfile;
      const stats = data.stats;

      if (stats) {
        this.setData({
          statCards: [
            { label: '帮助次数', value: stats.helpCount, unit: '次', icon: 'help' },
            { label: '获得帮助', value: stats.helpedCount, unit: '次', icon: 'helped' },
            { label: '技能数量', value: stats.skillCount, unit: '个', icon: 'skill' },
            { label: '好评率', value: stats.praiseRate, unit: '%', icon: 'praise' },
          ],
        });
      }
    } catch (err) {
      console.error('[MyStats] 获取统计数据失败:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.loadStats().finally(() => {
      wx.stopPullDownRefresh();
    });
  },
});
