/**
 * 设置页
 * 清除缓存、关于、退出登录
 */

import { logout } from '../../../utils/auth';

Page({
  data: {
    /** 退出登录弹窗 */
    showLogoutDialog: false,
  },

  /** 清除缓存 */
  onClearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '将清除本地缓存数据，不会影响账号信息。确定继续？',
      success: (res) => {
        if (res.confirm) {
          try {
            // 保留登录态相关的 key
            const token = wx.getStorageSync('access_token');
            const refreshToken = wx.getStorageSync('refresh_token');
            const userInfo = wx.getStorageSync('user_info');

            wx.clearStorageSync();

            // 恢复登录态
            if (token) wx.setStorageSync('access_token', token);
            if (refreshToken) wx.setStorageSync('refresh_token', refreshToken);
            if (userInfo) wx.setStorageSync('user_info', userInfo);

            wx.showToast({ title: '缓存已清除', icon: 'success' });
          } catch (err) {
            console.error('[Settings] 清除缓存失败:', err);
            wx.showToast({ title: '清除失败', icon: 'none' });
          }
        }
      },
    });
  },

  /** 打开关于页面 */
  onAbout() {
    wx.navigateTo({
      url: '/subpackages/user/about/about',
      fail() {
        wx.showToast({ title: '页面开发中', icon: 'none' });
      },
    });
  },

  /** 显示退出登录确认弹窗 */
  onLogout() {
    this.setData({ showLogoutDialog: true });
  },

  /** 确认退出登录 */
  onConfirmLogout() {
    this.setData({ showLogoutDialog: false });
    logout();
  },

  /** 取消退出登录 */
  onCancelLogout() {
    this.setData({ showLogoutDialog: false });
  },
});
