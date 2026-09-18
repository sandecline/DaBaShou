/**
 * 登录页
 * 独立的账号密码登录页面，替代原来的 wx.showModal 方案
 */

import { login } from '../../utils/auth';

Page({
  data: {
    username: '',
    password: '',
    showPassword: false,
    loading: false,
  },

  /** 返回上一页 */
  onBack() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
    } else {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  /** 用户名输入 */
  onUsernameInput(e: WechatMiniprogram.Input) {
    this.setData({ username: e.detail.value });
  },

  /** 密码输入 */
  onPasswordInput(e: WechatMiniprogram.Input) {
    this.setData({ password: e.detail.value });
  },

  /** 切换密码可见性 */
  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword });
  },

  /** 聚焦密码框（用户名确认后自动跳转） */
  focusPassword() {
    // 小程序无法直接 focus，通过 id 获取
    // 用户按 next 时自然会聚焦到下一个输入框
  },

  /** 快速填充测试账号 */
  fillAccount(e: WechatMiniprogram.TouchEvent) {
    const { u, p } = e.currentTarget.dataset as { u: string; p: string };
    this.setData({ username: u, password: p });
  },

  /** 登录 */
  async onLogin() {
    const { username, password } = this.data;

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    this.setData({ loading: true });

    try {
      await login(username.trim(), password);
      wx.showToast({ title: '登录成功', icon: 'success' });

      // 延迟返回，让用户看到成功提示
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) {
          wx.navigateBack();
        } else {
          wx.switchTab({ url: '/pages/mine/mine' });
        }
      }, 800);
    } catch (err: unknown) {
      console.error('[Login] 登录失败:', err);
      const msg = (err as { message?: string }).message || '登录失败，请检查账号密码';
      wx.showToast({ title: msg, icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
});
