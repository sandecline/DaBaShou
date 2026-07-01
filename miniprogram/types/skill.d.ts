/**
 * 技能相关类型定义
 * 对应后端 dabashou-skill 模块
 */

/** 技能分类 */
export interface SkillCategory {
  id: number;
  name: string;
  icon: string;
  sortOrder: number;
  status?: 0 | 1;
  tags?: SkillTag[];
  /** 前端 tree 结构兼容：子分类 */
  children?: SkillCategory[];
}

/** 技能标签 */
export interface SkillTag {
  id: number;
  categoryId: number;
  name: string;
  status: 0 | 1; // 0-禁用 1-正常
  createTime?: string;
  /** 前端附加字段 */
  categoryName?: string;
}

/** 用户技能 */
export interface UserSkill {
  id: number;
  userId?: number;
  skillTagId: number;
  /** 熟练度 */
  proficiency: Proficiency;
  /** 技能描述 */
  description: string;
  createTime?: string;
  /** 标签名 */
  tagName?: string;
  /** 分类名 */
  categoryName?: string;
}

/** 熟练度 */
export type Proficiency = 1 | 2 | 3 | 4;
// 1-了解  2-熟悉  3-精通  4-专家

export const ProficiencyMap: Record<Proficiency, string> = {
  1: '了解',
  2: '熟悉',
  3: '精通',
  4: '专家',
};

/** 服务方式 */
export type LocationType = 1 | 2 | 3;
// 1-线上  2-线下  3-均可

/** 技能货架（已上架的服务）
 *  已迁移至 types/shelf.d.ts，此处保留别名以兼容旧页面
 */
export type { SkillShelf, SkillShelfForm, ShelfStatus, ShelfSearchParams, TimeSlot } from './shelf';

/** 发布技能/服务参数（兼容旧页面，建议后续使用 SkillShelfForm） */
export interface PublishSkillParams {
  skillTagId: number;
  title: string;
  description: string;
  pointPrice: number;
  durationMinutes: number;
  locationType: LocationType;
  /** 图片（小程序扩展字段） */
  images?: string[];
  /** 空闲时段（与前端 SkillShelfForm 对齐） */
  timeSlots?: TimeSlot[];
}

/** 技能搜索参数 */
export interface SkillSearchParams {
  keyword?: string;
  categoryId?: number;
  skillTagId?: number;
  locationType?: LocationType;
  sortBy?: 'default' | 'price-asc' | 'price-desc' | 'newest';
  /** 分页 */
  pageNum: number;
  pageSize: number;
}
