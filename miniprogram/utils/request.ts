/**
 * 统一网络请求封装
 * 基于 wx.request 的 Promisify 封装
 * 功能：自动带 Token、401 自动刷新、统一错误处理
 */

import type { ApiResponse } from '../types/api-response';
import { API_BASE_URL } from '../config/api';

const BASE_URL = API_BASE_URL;
const TOKEN_KEY = 'dabashou_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

/** Token 刷新最大重试次数 */
const MAX_REFRESH_RETRIES = 3;

/**
 * 移除 data 中的 undefined/null 值，避免 wx.request 序列化为字符串 "undefined"
 * 数组参数（如 status=[1,3]）会被序列化为重复键: status=1&status=3
 */
function cleanParams(data?: Record<string, unknown>): Record<string, unknown> {
  if (!data) return {};
  const cleaned: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(data)) {
    if (v === undefined || v === null) continue;
    if (Array.isArray(v)) {
      if (v.length > 0) cleaned[k] = v;
    } else {
      cleaned[k] = v;
    }
  }
  return cleaned;
}

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, unknown>;
  header?: Record<string, string>;
  showLoading?: boolean;
  loadingText?: string;
}

// =====================================================================
//                                核心请求
// =====================================================================

function request<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const isAuthEndpoint = options.url.includes('/v1/auth/');
    const token = isAuthEndpoint ? '' : wx.getStorageSync(TOKEN_KEY);
    if (options.showLoading) {
      wx.showLoading({ title: options.loadingText || '加载中...', mask: true });
    }

    const cleanedData = cleanParams(options.data);

    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: cleanedData,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success(res) {
        if (res.statusCode === 401) {
          if (isAuthEndpoint) {
            const body = res.data as { msg?: string };
            reject({ code: 401, msg: body?.msg || '用户名或密码错误', data: res.data });
            return;
          }
          console.log('[Request] Token 过期，尝试刷新...');
          return refreshTokenAndRetry<T>(options).then(resolve).catch(reject);
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data as ApiResponse<T>;
          if (body?.code === 401 && !isAuthEndpoint) {
            console.log('[Request] Token 过期，尝试刷新...');
            return refreshTokenAndRetry<T>(options).then(resolve).catch(reject);
          }
          if (body && typeof body.code === 'number' && body.code !== 200) {
            reject(body);
            return;
          }
          resolve(body);
        } else {
          const body = res.data as { msg?: string };
          reject({ code: res.statusCode, msg: body?.msg || '请求失败', data: res.data });
        }
      },
      fail(err) {
        console.error('[Request] 网络错误:', err);
        reject({ code: -1, msg: '网络连接失败，请稍后重试', detail: err });
      },
      complete() {
        if (options.showLoading) wx.hideLoading();
      },
    });
  });
}

let _refreshRetryCount = 0;
let _isReLaunching = false;
let _isRefreshing = false;
let _refreshQueue: Array<{ resolve: (value: ApiResponse<unknown>) => void; reject: (reason?: unknown) => void; options: RequestOptions }> = [];

function safeReLaunch() {
  if (_isReLaunching) return;
  _isReLaunching = true;
  const app = getApp();
  app.clearSession();
  wx.reLaunch({ url: '/pages/index/index', fail() { _isReLaunching = false; } });
}

async function refreshTokenAndRetry<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  if (_isRefreshing) {
    return new Promise<ApiResponse<T>>((resolve, reject) => {
      _refreshQueue.push({ resolve: resolve as (value: ApiResponse<unknown>) => void, reject, options });
    });
  }

  _isRefreshing = true;
  _refreshRetryCount++;
  if (_refreshRetryCount > MAX_REFRESH_RETRIES) {
    _refreshRetryCount = 0;
    _isRefreshing = false;
    _refreshQueue.forEach((q) => { q.reject({ code: 401, msg: '登录已过期，请重新登录' }); });
    _refreshQueue = [];
    safeReLaunch();
    throw { code: 401, msg: '登录已过期，请重新登录' };
  }
  try {
    const refreshToken = wx.getStorageSync(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      _isRefreshing = false;
      _refreshQueue.forEach((q) => { q.reject({ code: 401, msg: '登录已过期，请重新登录' }); });
      _refreshQueue = [];
      safeReLaunch();
      throw { code: 401, msg: '登录已过期，请重新登录' };
    }
    const res = await rawRequest<{ accessToken: string; refreshToken: string }>({
      url: '/v1/auth/refresh',
      method: 'POST',
      data: { refreshToken },
    });
    wx.setStorageSync(TOKEN_KEY, res.data.accessToken);
    wx.setStorageSync(REFRESH_TOKEN_KEY, res.data.refreshToken);
    _refreshRetryCount = 0;
    _isRefreshing = false;
    const queue = _refreshQueue;
    _refreshQueue = [];
    queue.forEach((q) => {
      request<unknown>(q.options).then(q.resolve).catch(q.reject);
    });
    return request<T>(options);
  } catch (err) {
    _refreshRetryCount = 0;
    _isRefreshing = false;
    _refreshQueue.forEach((q) => { q.reject(err); });
    _refreshQueue = [];
    throw err;
  }
}

function rawRequest<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const isAuthEndpoint = options.url.includes('/v1/auth/');
    const token = isAuthEndpoint ? '' : wx.getStorageSync(TOKEN_KEY);
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'POST',
      data: cleanParams(options.data),
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data as ApiResponse<T>;
          if (body && typeof body.code === 'number' && body.code !== 200) {
            reject(body);
            return;
          }
          resolve(body);
        }
        else {
          const body = res.data as { msg?: string };
          reject({ code: res.statusCode, msg: body?.msg || '刷新失败' });
        }
      },
      fail(err) { reject({ code: -1, msg: '网络错误', detail: err }); },
    });
  });
}

// =====================================================================
//                            便捷方法
// =====================================================================

export const api = {
  get<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'GET', data }); },
  post<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'POST', data }); },
  put<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'PUT', data }); },
  delete<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'DELETE', data }); },
};

export { request, rawRequest, BASE_URL };
