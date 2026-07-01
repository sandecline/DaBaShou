/**
 * 技能货架相关类型定义
 * 对应后端 dabashou-shelf 模块
 * 与 frontend/src/types/shelf.ts 保持一致
 */

/** 时间格子 */
export interface TimeSlot {
  id?: number;
  userId?: number;
  date: string; // YYYY-MM-DD
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  status?: 0 | 1 | 2; // 0-不可用 1-可预约 2-已预约
  createTime?: string;
  updateTime?: string;
}

/** 技能货架（已上架的服务） */
export interface SkillShelf {
  id: number;
  userId: number;
  skillTagId: number;
  /** 服务标题 */
  title: string;
  /** 服务描述 */
  description: string;
  /** 积分价格 */
  pointPrice: number;
  /** 预计时长（分钟） */
  durationMinutes: number;
  /** 服务方式 */
  locationType: 1 | 2 | 3;
  /** 状态 */
  status: 0 | 1 | 2;
  /** 封面图（与前端一致） */
  coverImage?: string;
  /** 技能标签名 */
  tagName?: string;
  /** 分类名 */
  categoryName?: string;
  /** 距离（米） */
  distance?: number;
  /** 热度分 */
  heatScore?: number;
  /** 用户昵称 */
  nickname?: string;
  /** 用户头像 */
  avatar?: string;
  /** 用户名称（前端兼容字段） */
  userName?: string;
  /** 用户头像（前端兼容字段） */
  userAvatar?: string;
  /** 信任分 */
  trustScore?: number;
  createTime: string;
  updateTime?: string;
}

/** 货架详情（扩展） */
export interface ShelfDetail extends SkillShelf {
  /** 空闲时段 */
  timeSlots?: TimeSlot[];
  /** 服务方式描述 */
  locationTypeDesc?: string;
  /** 状态描述 */
  statusDesc?: string;
}

/** 技能服务发布/编辑表单 */
export interface SkillShelfForm {
  skillTagId: number | null;
  title: string;
  description: string;
  pointPrice: number;
  durationMinutes: number;
  locationType: 1 | 2 | 3;
  timeSlots: TimeSlot[];
  /** 图片（小程序扩展字段，后端兼容） */
  images?: string[];
}

/** 货架搜索参数 */
export interface ShelfSearchParams {
  keyword?: string;
  categoryId?: number;
  skillTagId?: number;
  locationType?: 1 | 2 | 3;
  sortBy?: 'default' | 'price-asc' | 'price-desc' | 'newest' | 'heat';
  longitude?: number;
  latitude?: number;
  pageNum: number;
  pageSize: number;
}

/** 货架状态 */
export type ShelfStatus = 0 | 1 | 2;
// 0-下架  1-上架  2-审核中
