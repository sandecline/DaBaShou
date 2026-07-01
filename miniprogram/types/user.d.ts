/**
 * 用户相关类型定义
 * 对应后端 dabashou-user 模块
 */

/** 用户基本信息（列表展示用） */
export interface UserBrief {
  id: number;
  nickname: string;
  avatar: string;
  trustScore: number;
  /** 信任分等级标签 */
  trustLevel: '新人' | '靠谱' | '金牌';
}

/** 用户详细信息（个人主页） */
export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  phone: string;
  email: string;
  pointBalance: number;
  trustScore: number;
  trustLevel: '新人' | '靠谱' | '金牌';
  campus: string;
  building: string;
  bio: string;
  /** 校园认证状态 */
  campusAuthStatus: CampusAuthStatus;
  /** 统计 */
  stats: UserStats;
  createTime: string;
}

/** 校园认证状态 */
export type CampusAuthStatus = 0 | 1 | 2;
// 0-待审核  1-已通过  2-已拒绝

/** 用户统计 */
export interface UserStats {
  /** 帮助次数 */
  helpCount: number;
  /** 获得求助次数 */
  helpedCount: number;
  /** 发布技能数 */
  skillCount: number;
  /** 好评率 */
  praiseRate: number;
  /** 注册天数 */
  registerDays: number;
}

/** 编辑用户信息参数 */
export interface UpdateProfileParams {
  nickname?: string;
  avatar?: string;
  campus?: string;
  building?: string;
  bio?: string;
}
