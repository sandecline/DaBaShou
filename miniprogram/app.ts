/**
 * 搭把手 微信小程序 — App 入口
 * 全局生命周期、全局数据、事件总线
 */

import type { IAppOption } from './types/global';

App<IAppOption>({
  // ========== 全局数据 ==========
  globalData: {
    userInfo: null,
    token: '',
    isLoggedIn: false,
    unreadCount: 0,
  },

  // ========== 生命周期 ==========

  /**
   * 小程序初始化
   * 触发时机：小程序首次启动
   */
  onLaunch() {
    console.log('[App] 搭把手小程序启动');
    this.restoreSession();
  },

  /**
   * 小程序显示（从后台切回前台）
   */
  onShow() {
    console.log('[App] 小程序显示');
  },

  /**
   * 小程序隐藏（切到后台）
   */
  onHide() {
    console.log('[App] 小程序隐藏');
  },

  /**
   * 小程序报错
   */
  onError(msg: string) {
    console.error('[App] 全局错误:', msg);
  },

  // ========== 全局方法 ==========

  /**
   * 恢复登录态
   * 从 Storage 读取 token，验证有效性
   */
  restoreSession() {
    try {
      const token = wx.getStorageSync('access_token');
      const userInfo = wx.getStorageSync('user_info');
      if (token) {
        this.globalData.token = token;
        this.globalData.isLoggedIn = true;
        console.log('[App] 登录态已恢复');
      }
      if (userInfo) {
        this.globalData.userInfo = userInfo;
      }
    } catch (err) {
      console.error('[App] 恢复登录态失败:', err);
    }
  },

  /**
   * 清除登录态
   */
  clearSession() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
    wx.removeStorageSync('access_token');
    wx.removeStorageSync('refresh_token');
    wx.removeStorageSync('user_info');
  },
});
