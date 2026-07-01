/**
 * 技能服务
 * 对应后端 dabashou-skill 模块
 * 与 frontend/src/api/skill.ts 对齐
 */

import { api } from '../utils/request';
import type { ApiResponse, PageResult } from '../types/api-response';
import type { SkillCategory, SkillTag, UserSkill, Proficiency } from '../types/skill';

export const skillService = {
  /** 获取技能分类树 */
  getCategories() {
    return api.get<SkillCategory[]>('/v1/skills/categories/tree');
  },

  /** 获取技能标签列表（可选按分类筛选） */
  getTags(categoryId?: number) {
    return api.get<SkillTag[]>('/v1/skills/tags', categoryId ? { categoryId } as unknown as Record<string, unknown> : undefined);
  },

  /** 获取热门技能标签 */
  getHotTags(limit?: number) {
    return api.get<SkillTag[]>('/v1/skills/tags/hot', limit ? { limit } as unknown as Record<string, unknown> : undefined);
  },

  /** 获取我的技能列表 */
  getMySkills() {
    return api.get<UserSkill[]>('/v1/users/me/skills');
  },

  /** 添加我的技能 */
  addMySkill(data: { skillTagId: number; proficiency: Proficiency; description?: string }) {
    return api.post<{ id: number }>('/v1/users/me/skills', data as unknown as Record<string, unknown>);
  },

  /** 更新我的技能 */
  updateMySkill(id: number, data: { proficiency?: Proficiency; description?: string }) {
    return api.put<void>(`/v1/users/me/skills/${id}`, data as unknown as Record<string, unknown>);
  },

  /** 删除我的技能 */
  deleteMySkill(id: number) {
    return api.delete<void>(`/v1/users/me/skills/${id}`);
  },
};
