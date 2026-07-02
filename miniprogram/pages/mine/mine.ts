/**
 * 个人中心 — 我的 Tab 页面
 * 用户信息展示 + 功能菜单入口
 */

import { userService } from '../../services/user';
import { login } from '../../utils/auth';
import type { UserProfile } from '../../types/user';

interface MenuItem {
  title: string;
  icon: string;
  url: string;
}

/** 信任分等级颜色映射（模块级常量，避免每次setData重建） */
const TRUST_COLOR_MAP: Record<string, string> = {
  '金牌': 'primary',
  '靠谱': 'success',
  '新人': 'warning',
};

Page({
  data: {
    /** 用户信息 */
    userInfo: null as UserProfile | null,
    /** 是否已登录 */
    isLoggedIn: false,
    /** 信任分标签颜色映射 */
    trustLevelColor: 'primary' as string,
    /** 功能菜单 */
    menuList: [] as MenuItem[],
  },

  onLoad() {
    this.setData({
      menuList: [
        {
          title: '我的订单',
          icon: 'order',
          url: '/subpackages/user/order-list/order-list',
        },
        {
          title: '我的小铺',
          icon: 'shop',
          url: '/subpackages/user/shelf-manage/shelf-manage',
        },
        {
          title: '我的求助',
          icon: 'help-circle',
          url: '/subpackages/user/demand-manage/demand-manage',
        },
        {
          title: '信用评价',
          icon: 'secured',
          url: '/subpackages/user/credit/credit',
        },
        {
          title: '数据统计',
          icon: 'chart-bar',
          url: '/subpackages/user/my-stats/my-stats',
        },
        {
          title: '编辑资料',
          icon: 'user',
          url: '/subpackages/user/profile-edit/profile-edit',
        },
        {
          title: '积分中心',
          icon: 'wallet',
          url: '/subpackages/user/point-detail/point-detail',
        },
        {
          title: '设置',
          icon: 'setting',
          url: '/subpackages/user/settings/settings',
        },
      ],
    });
  },

  onShow() {
    this.loadUserInfo();
  },

  /** 加载用户信息 */
  loadUserInfo() {
    const app = getApp();
    const globalUser = app.globalData.userInfo;

    if (globalUser && app.globalData.isLoggedIn) {
      this.setUserData(globalUser);
      // 后台静默刷新最新数据
      this.fetchRemoteProfile();
    } else {
      this.setData({
        userInfo: null,
        isLoggedIn: false,
      });
    }
  },

  /** 从后端获取最新用户信息 */
  async fetchRemoteProfile() {
    try {
      const res = await userService.getProfile();
      const data = res.data || res;
      const app = getApp();
      app.globalData.userInfo = data;
      wx.setStorageSync('dabashou_user', JSON.stringify(data));
      this.setUserData(data);
    } catch (err) {
      console.error('[Mine] 获取用户信息失败:', err);
    }
  },

  /** 设置用户数据并确定信任分标签颜色 */
  setUserData(user: UserProfile) {
    this.setData({
      userInfo: { ...user, avatar: user.avatar || '' },
      isLoggedIn: true,
      trustLevelColor: TRUST_COLOR_MAP[user.trustLevel] || 'warning',
    });
  },

  /** 点击菜单项 — 跳转子包页面 */
  onMenuTap(e: WechatMiniprogram.TouchEvent) {
    const { url } = e.currentTarget.dataset as { url: string };
    if (!url) return;

    wx.navigateTo({
      url,
      fail() {
        wx.showToast({ title: '页面开发中', icon: 'none' });
      },
    });
  },

  /** 点击登录按钮 — 调起微信静默登录 */
  async onLogin() {
    try {
      const input = await new Promise<string>((resolve, reject) => {
        wx.showModal({
          title: '账号登录',
          editable: true,
          placeholderText: '请输入: 用户名 密码',
          confirmText: '登录',
          success(res) {
            if (res.confirm) resolve((res.content || '').trim());
            else reject(new Error('cancelled'));
          },
          fail: reject,
        });
      });
      const [username, password] = input.split(/\s+/);
      if (!username || !password) {
        wx.showToast({ title: '请输入用户名和密码', icon: 'none' });
        return;
      }
      wx.showLoading({ title: '登录中...', mask: true });
      await login(username, password);
      wx.hideLoading();

      const app = getApp();
      const user = app.globalData.userInfo;
      if (user) {
        this.setUserData(user);
      }

      wx.showToast({ title: '登录成功', icon: 'success' });
    } catch (err) {
      wx.hideLoading();
      console.error('[Mine] 登录失败:', err);
      wx.showToast({ title: '登录失败，请重试', icon: 'none' });
    }
  },

  onShareAppMessage() {
    return {
      title: '搭把手 - 校园技能共享平台',
      path: '/pages/index/index',
    };
  },
});
