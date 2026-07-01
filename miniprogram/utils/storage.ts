/**
 * 本地存储封装
 * 类型安全的 Storage 读写，统一键名管理
 */

/** 存储键名常量 */
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_INFO: 'user_info',
  USER_SETTINGS: 'user_settings',
  DRAFT_PUBLISH: 'draft_publish',
  SEARCH_HISTORY: 'search_history',
} as const;

/**
 * 类型安全的同步读取
 */
export function getStorage<T = unknown>(key: string, defaultValue?: T): T {
  try {
    const value = wx.getStorageSync(key);
    return value !== '' && value !== undefined ? (value as T) : (defaultValue as T);
  } catch (err) {
    console.error(`[Storage] 读取失败: ${key}`, err);
    return defaultValue as T;
  }
}

/**
 * 安全写入
 */
export function setStorage(key: string, value: unknown): boolean {
  try {
    wx.setStorageSync(key, value);
    return true;
  } catch (err) {
    console.error(`[Storage] 写入失败: ${key}`, err);
    return false;
  }
}

/**
 * 删除
 */
export function removeStorage(key: string): boolean {
  try {
    wx.removeStorageSync(key);
    return true;
  } catch (err) {
    console.error(`[Storage] 删除失败: ${key}`, err);
    return false;
  }
}

/**
 * 获取存储信息
 */
export function getStorageInfo(): WechatMiniprogram.GetStorageInfoSyncOption {
  return wx.getStorageInfoSync();
}

/**
 * 清空所有存储（退出登录时使用）
 */
export function clearAllStorage(): void {
  try {
    wx.clearStorageSync();
  } catch (err) {
    console.error('[Storage] 清空失败:', err);
  }
}

/**
 * 搜索历史管理
 */
const MAX_HISTORY = 10;

export function getSearchHistory(): string[] {
  return getStorage<string[]>(STORAGE_KEYS.SEARCH_HISTORY, []);
}

export function addSearchHistory(keyword: string): void {
  const history = getSearchHistory();
  const filtered = history.filter((item) => item !== keyword);
  filtered.unshift(keyword);
  setStorage(STORAGE_KEYS.SEARCH_HISTORY, filtered.slice(0, MAX_HISTORY));
}

export function clearSearchHistory(): void {
  setStorage(STORAGE_KEYS.SEARCH_HISTORY, []);
}
