/**
 * 登录认证 + Token 管理
 * 
 * 与 frontend/src/utils/auth.ts + frontend/src/stores/user.ts 严格对齐：
 * - 相同的 API 端点 /v1/auth/login、/v1/auth/sms-login、/v1/auth/refresh
 * - 相同的存储键 dabashou_token / dabashou_user
 * - 相同的 LoginVo 结构
 */

import { request, rawRequest } from './request';
import type { UserProfile } from '../types/user';

// ── 与 Web 前端一致的存储键 ──

const TOKEN_KEY = 'dabashou_token';
const USER_KEY = 'dabashou_user';

// ── LoginVo（与 frontend/src/types/api.ts LoginVo 一致） ──

export interface LoginVo {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;   // 秒，默认 86400（24h）
  userId: number;
  nickname: string;
  avatar: string;
}

// ── Token 工具（与 frontend/src/utils/auth.ts 对齐） ──

export function getToken(): string | null {
  try {
    const token = wx.getStorageSync(TOKEN_KEY);
    if (!token) return null;
    if (isTokenExpired(token)) {
      removeToken();
      removeUserInfo();
      return null;
    }
    return token;
  } catch {
    return null;
  }
}

export function setToken(token: string): void {
  wx.setStorageSync(TOKEN_KEY, token);
}

export function removeToken(): void {
  wx.removeStorageSync(TOKEN_KEY);
}

export function isTokenExpired(token: string): boolean {
  try {
    const payload = getTokenPayload(token);
    return (payload.exp as number) * 1000 <= Date.now();
  } catch {
    return true;
  }
}

function getTokenPayload(token: string): Record<string, unknown> {
  const parts = token.split('.');
  if (parts.length !== 3) throw new Error('Invalid JWT');
  const payload = parts[1]
    .replace(/-/g, '+')
    .replace(/_/g, '/');
  // 纯 JS base64 解码（兼容小程序无 atob）
  const pad = payload.length % 4;
  const padded = pad ? payload + '='.repeat(4 - pad) : payload;
  let str = '';
  const base64Chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
  const bits: number[] = [];
  for (let i = 0; i < padded.length; i++) {
    const c = padded.charAt(i);
    if (c === '=') break;
    const idx = base64Chars.indexOf(c);
    if (idx === -1) continue;
    const b = idx.toString(2).padStart(6, '0');
    bits.push(...b.split('').map(Number));
  }
  for (let i = 0; i + 7 < bits.length; i += 8) {
    let byte = 0;
    for (let j = 0; j < 8; j++) byte = (byte << 1) | bits[i + j];
    str += String.fromCharCode(byte);
  }
  return JSON.parse(str);
}

// ── 用户信息（与 frontend/src/utils/auth.ts 对齐） ──

export function getUserInfo(): UserProfile | null {
  try {
    const raw = wx.getStorageSync(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setUserInfo(user: UserProfile): void {
  wx.setStorageSync(USER_KEY, JSON.stringify(user));
}

export function removeUserInfo(): void {
  wx.removeStorageSync(USER_KEY);
}

export function isLoggedIn(): boolean {
  return !!getToken();
}

/** 获取当前用户 ID（兼容 globalData / Storage 兜底） */
export function getCurrentUserId(): number {
  try {
    const app = getApp();
    if (app.globalData?.userInfo?.id) {
      return app.globalData.userInfo.id as number;
    }
    const user = getUserInfo();
    if (user?.id) return user.id;
  } catch {
    // ignore
  }
  return 0;
}


// ── 密码登录（与 frontend/src/api/auth.ts login() 一致） ──

export async function login(username: string, password: string): Promise<UserProfile> {
  const res = await request<LoginVo>({
    url: '/v1/auth/login',
    method: 'POST',
    data: { username, password } as Record<string, unknown>,
  });
  return saveLoginResult(res.data, username);
}

// ── 短信登录（与 frontend/src/api/auth.ts smsLogin() 一致） ──

export async function sendSmsCode(phone: string): Promise<void> {
  await request<void>({
    url: '/v1/auth/sms-code',
    method: 'POST',
    data: { phone } as Record<string, unknown>,
  });
}

export async function smsLogin(phone: string, code: string): Promise<UserProfile> {
  const res = await request<LoginVo>({
    url: '/v1/auth/sms-login',
    method: 'POST',
    data: { phone, code } as Record<string, unknown>,
  });
  return saveLoginResult(res.data, phone);
}

// ── Token 刷新（与 frontend/src/api/auth.ts refreshToken() 一致） ──

export async function refreshAuthToken(): Promise<LoginVo | null> {
  try {
    const refreshToken = wx.getStorageSync('refresh_token');
    if (!refreshToken) return null;
    const res = await rawRequest<LoginVo>({
      url: '/v1/auth/refresh',
      method: 'POST',
      data: { refreshToken } as Record<string, unknown>,
    });
    if (res.code === 200 && res.data) {
      saveLoginResult(res.data, '', false);
      return res.data;
    }
    return null;
  } catch {
    return null;
  }
}

// ── 保存登录结果（与 frontend/src/stores/user.ts login() 对齐） ──

function saveLoginResult(vo: LoginVo, username: string, skipRefresh?: boolean): UserProfile {
  // 1. 存储 token
  setToken(vo.accessToken);
  if (!skipRefresh && vo.refreshToken) {
    wx.setStorageSync('refresh_token', vo.refreshToken);
  }

  // 2. 构建用户对象（与 Web 端一致的字段）
  const user: UserProfile = {
    id: vo.userId,
    nickname: vo.nickname,
    avatar: vo.avatar || '',
    username: username || '',
    phone: '',
    email: '',
    campus: '',
    building: '',
    bio: '',
    trustLevel: '新人',
    trustScore: 0,
    campusAuthStatus: 0,
    pointBalance: 0,
    createTime: '',
  } as UserProfile;

  // 3. 存储用户信息
  setUserInfo(user);

  // 4. 更新全局状态
  const app = getApp();
  app.globalData.token = vo.accessToken;
  app.globalData.userInfo = user;
  app.globalData.isLoggedIn = true;

  // 5. 异步获取完整资料（与 Web 端 fetchProfile() 一致，非阻塞）
  fetchProfile().catch(() => {});

  return user;
}

// ── 获取完整用户资料（与 frontend/src/stores/user.ts fetchProfile() 一致） ──

async function fetchProfile(): Promise<void> {
  try {
    const res = await request<UserProfile>({
      url: '/v1/user/profile',
      method: 'GET',
    });
    if (res.data) {
      setUserInfo(res.data);
      const app = getApp();
      app.globalData.userInfo = res.data;
    }
  } catch (err) {
    console.warn('[Auth] 获取用户资料失败（非阻塞）:', err);
  }
}

// ── 确保登录（页面级调用） ──

export async function ensureLogin(): Promise<boolean> {
  const app = getApp();
  if (app.globalData.isLoggedIn && app.globalData.token) {
    return true;
  }

  // 尝试从 Storage 恢复
  const token = getToken();
  if (token) {
    app.globalData.token = token;
    app.globalData.isLoggedIn = true;
    const userInfo = getUserInfo();
    if (userInfo) app.globalData.userInfo = userInfo;
    return true;
  }

  // 未登录，跳转登录页
  wx.navigateTo({ url: '/pages/login/login' });
  return false;
}

// ── 退出登录（与 frontend/src/utils/auth.ts logout() 一致） ──

export function logout(): void {
  const app = getApp();
  removeToken();
  removeUserInfo();
  wx.removeStorageSync('refresh_token');
  app.globalData.token = '';
  app.globalData.userInfo = null;
  app.globalData.isLoggedIn = false;
}

