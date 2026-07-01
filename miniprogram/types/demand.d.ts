/**
 * 需求相关类型定义
 * 对应后端 dabashou-demand 模块
 */

/** 需求（求助） */
export interface Demand {
  id: number;
  userId: number;
  skillTagId: number | null;
  /** 技能标签名 */
  skillTagName?: string;
  /** 求助标题 */
  title: string;
  /** 求助描述 */
  description: string;
  /** 悬赏积分 */
  pointReward: number;
  /** 截止时间 */
  deadline: string | null;
  /** 需求类型 */
  demandType: DemandType;
  /** 服务方式 */
  locationType: import('./skill').LocationType;
  /** 是否急单（12h 内截止） */
  isUrgent?: boolean;
  /** 状态描述 */
  statusDesc?: string;
  /** 需求类型描述 */
  demandTypeDesc?: string;
  /** 关联信息 */
  userInfo?: import('./user').UserBrief;
  /** 用户名称（前端兼容字段） */
  userName?: string;
  /** 用户头像（前端兼容字段） */
  userAvatar?: string;
  /** 校区 */
  campus?: string;
  /** 楼栋 */
  building?: string;
  /** 距离（米） */
  distance?: number;
  images: string[];
  createTime: string;
  updateTime?: string;
}

/** 需求类型 */
export type DemandType = 1 | 2;
// 1-求助悬赏  2-技能置换

/** 需求状态 */
export type DemandStatus = 0 | 1 | 2 | 3;
// 0-已关闭  1-待接单  2-进行中  3-已完成

/** 发布需求参数 */
export interface PublishDemandParams {
  skillTagId: number | null;
  title: string;
  description: string;
  pointReward: number;
  deadline: string | null;
  demandType: DemandType;
  locationType: import('./skill').LocationType;
  /** 是否急单 */
  isUrgent?: boolean;
  /** 图片（可选） */
  images?: string[];
  /** 经度（线下服务时可选） */
  longitude?: number;
  /** 纬度（线下服务时可选） */
  latitude?: number;
}

/** 需求表单（与前端 DemandForm 对齐） */
export interface DemandForm {
  skillTagId: number | null;
  title: string;
  description: string;
  pointReward: number;
  deadline: string | null;
  locationType: import('./skill').LocationType;
  isUrgent?: boolean;
  demandType?: DemandType;
  longitude?: number;
  latitude?: number;
}

/** 需求搜索参数 */
export interface DemandSearchParams {
  keyword?: string;
  skillTagId?: number;
  locationType?: import('./skill').LocationType;
  status?: DemandStatus;
  sortBy?: 'default' | 'reward-desc' | 'deadline' | 'newest';
  pageNum: number;
  pageSize: number;
}
