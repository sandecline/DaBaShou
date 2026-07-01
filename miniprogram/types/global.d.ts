/**
 * 全局类型声明
 * app.globalData 类型、扩展方法等
 */

import type { UserProfile } from './user';

/** App 全局数据接口 */
export interface IAppOption {
  globalData: {
    /** 当前用户信息 */
    userInfo: UserProfile | null;
    /** JWT Token */
    token: string;
    /** 是否已登录 */
    isLoggedIn: boolean;
    /** 未读消息总数 */
    unreadCount: number;
  };
  /** 恢复登录态 */
  restoreSession(): void;
  /** 清除登录态 */
  clearSession(): void;
}

/** 信任分等级 */
export type TrustLevel = '新人' | '靠谱' | '金牌';

/** 根据信任分获取等级 */
export function getTrustLevel(score: number): TrustLevel;
