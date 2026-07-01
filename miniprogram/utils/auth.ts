/**
 * 登录认证 + Token 管理
 * 微信静默登录流程：wx.login → 后端换 JWT → 存储
 */

import { request, api } from './request';
import type { UserProfile } from '../types/user';

/**
 * 微信静默登录
 * 调用 wx.login 获取临时 code，发给后端换 JWT Token
 */
export async function silentLogin(): Promise<UserProfile> {
  try {
    // 1. 调用微信登录，获取临时 code
    const loginRes = await wxLogin();
    const { code } = loginRes;

    // 2. 后端用 code 换取 openid，生成 JWT
    const res = await request<{
      access_token: string;
      refresh_token: string;
      user: UserProfile;
    }>({
      url: '/api/v1/auth/wechat-login',
      method: 'POST',
      data: { code },
    });

    // 3. 存储 token
    wx.setStorageSync('access_token', res.data.access_token);
    wx.setStorageSync('refresh_token', res.data.refresh_token);
    wx.setStorageSync('user_info', res.data.user);

    // 4. 更新全局状态
    const app = getApp();
    app.globalData.token = res.data.access_token;
    app.globalData.userInfo = res.data.user;
    app.globalData.isLoggedIn = true;

    return res.data.user;
  } catch (err) {
    console.error('[Auth] 静默登录失败:', err);
    throw err;
  }
}

/**
 * 微信登录（Promise 封装）
 */
function wxLogin(): Promise<WechatMiniprogram.LoginSuccessCallbackResult> {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res);
        } else {
          reject(new Error('wx.login 未返回 code'));
        }
      },
      fail(err) {
        reject(err);
      },
    });
  });
}

/**
 * 获取用户手机号
 * 需通过 button open-type="getPhoneNumber" 触发
 * @param e - getPhoneNumber 事件对象
 */
export async function bindPhone(e: WechatMiniprogram.ButtonGetPhoneNumber): Promise<string> {
  const { code, errMsg } = e.detail;

  if (errMsg !== 'getPhoneNumber:ok' || !code) {
    console.log('[Auth] 用户拒绝手机号授权');
    return '';
  }

  try {
    const res = await request<{ phone: string }>({
      url: '/api/v1/auth/bind-phone',
      method: 'POST',
      data: { code },
    });
    return res.data.phone;
  } catch (err) {
    console.error('[Auth] 手机号绑定失败:', err);
    throw err;
  }
}

/**
 * 检查登录态
 * 如果 token 过期或不存在，自动重新登录
 */
export async function ensureLogin(): Promise<boolean> {
  const app = getApp();
  if (app.globalData.isLoggedIn && app.globalData.token) {
    return true;
  }

  // 尝试从 Storage 恢复
  const storedToken = wx.getStorageSync('access_token');
  if (storedToken) {
    app.globalData.token = storedToken;
    app.globalData.isLoggedIn = true;
    const userInfo = wx.getStorageSync('user_info');
    if (userInfo) {
      app.globalData.userInfo = userInfo;
    }
    return true;
  }

  // 需要重新登录
  try {
    await silentLogin();
    return true;
  } catch {
    return false;
  }
}

/**
 * 退出登录
 */
export function logout(): void {
  const app = getApp();
  app.clearSession();
  wx.reLaunch({ url: '/pages/index/index' });
}
