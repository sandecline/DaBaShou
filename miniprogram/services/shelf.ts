/**
 * 技能货架服务
 * 对应后端 dabashou-shelf 模块
 * 与 frontend/src/api/shelf.ts 对齐
 */

import { api } from '../utils/request';
import type { ApiResponse, PageResult } from '../types/api-response';
import type { SkillShelf, ShelfDetail, ShelfSearchParams, SkillShelfForm, TimeSlot } from '../types/shelf';

export const shelfService = {
  /** 搜索技能货架列表 */
  search(params: ShelfSearchParams) {
    return api.get<PageResult<SkillShelf>>('/v1/shelves', params as unknown as Record<string, unknown>);
  },

  /** 获取货架详情 */
  getDetail(shelfId: number) {
    return api.get<ShelfDetail>(`/v1/shelves/${shelfId}`);
  },

  /** 发布技能服务到货架 */
  publish(params: SkillShelfForm) {
    return api.post<{ id: number }>('/v1/shelves', params as unknown as Record<string, unknown>);
  },

  /** 更新货架服务 */
  update(shelfId: number, params: Partial<SkillShelfForm>) {
    return api.put<SkillShelf>(`/v1/shelves/${shelfId}`, params as unknown as Record<string, unknown>);
  },

  /** 上架 */
  onShelf(shelfId: number) {
    return api.put<void>(`/v1/shelves/${shelfId}/on`);
  },

  /** 下架 */
  offShelf(shelfId: number) {
    return api.put<void>(`/v1/shelves/${shelfId}/off`);
  },

  /** 删除货架服务 */
  deleteShelf(shelfId: number) {
    return api.delete<void>(`/v1/shelves/${shelfId}`);
  },

  /** 获取我的货架列表 */
  getMine(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<SkillShelf>>('/v1/shelves/mine', params as unknown as Record<string, unknown>);
  },

  /** 获取用户货架列表 */
  getUserShelves(userId: number, params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<SkillShelf>>(`/v1/users/${userId}/shelves`, params as unknown as Record<string, unknown>);
  },

  /** 设置空闲时段 */
  setTimeSlots(shelfId: number, slots: TimeSlot[]) {
    return api.post<void>(`/v1/shelves/${shelfId}/timeslots`, slots as unknown as Record<string, unknown>);
  },

  /** 获取空闲时段 */
  getTimeSlots(shelfId: number) {
    return api.get<TimeSlot[]>(`/v1/shelves/${shelfId}/timeslots`);
  },

  /** 删除空闲时段 */
  deleteTimeSlot(shelfId: number, slotId: number) {
    return api.delete<void>(`/v1/shelves/${shelfId}/timeslots/${slotId}`);
  },
};
