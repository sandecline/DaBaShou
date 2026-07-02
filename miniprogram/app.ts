/**
 * 搭把手 微信小程序 — App 入口
 * 全局生命周期、全局数据、事件总线
 * 
 * auth 与 frontend/src/utils/auth.ts 严格对齐：
 * - 相同的存储键 dabashou_token / dabashou_user
 * - 相同的 LoginVo 结构
 */

import type { IAppOption } from './types/global';
import { USE_MOCK } from './config/api';

App<IAppOption>({
  // ========== 全局数据 ==========
  globalData: {
    userInfo: null,
    token: '',
    isLoggedIn: false,
    unreadCount: 0,
    mockMode: USE_MOCK,
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
   * 与 Web 端一致的存储键
   */
  restoreSession() {
    try {
      const token = wx.getStorageSync('dabashou_token');
      const userInfo = wx.getStorageSync('dabashou_user');
      if (token) {
        this.globalData.token = token;
        this.globalData.isLoggedIn = true;
        console.log('[App] 登录态已恢复');
      }
      if (userInfo) {
        try {
          this.globalData.userInfo = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo;
        } catch {
          this.globalData.userInfo = userInfo;
        }
      }
    } catch (err) {
      console.error('[App] 恢复登录态失败:', err);
    }
  },

  /**
   * 清除登录态
   * 与 Web 端一致的存储清理
   */
  clearSession() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
    wx.removeStorageSync('dabashou_token');
    wx.removeStorageSync('dabashou_user');
    wx.removeStorageSync('refresh_token');
  },
});
