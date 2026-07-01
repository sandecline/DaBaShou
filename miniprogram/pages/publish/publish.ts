/**
 * 发布页 — 选择发布技能 or 发布求助
 */

import { silentLogin } from '../../utils/auth';

Page({
  data: {
    /** 是否已登录 */
    ready: false,
  },

  onLoad() {
    this.doLogin();
  },

  /** 静默登录 */
  async doLogin() {
    try {
      await silentLogin();
    } catch (err) {
      console.error('[Publish] 静默登录失败:', err);
    } finally {
      this.setData({ ready: true });
    }
  },

  /** 跳转发布技能 */
  goPublishSkill() {
    wx.navigateTo({ url: '/pages/publish-skill/publish-skill' });
  },

  /** 跳转发布求助 */
  goPublishDemand() {
    wx.navigateTo({ url: '/pages/publish-demand/publish-demand' });
  },

  onShareAppMessage() {
    return {
      title: '搭把手 - 发布技能或求助',
      path: '/pages/publish/publish',
    };
  },
});
