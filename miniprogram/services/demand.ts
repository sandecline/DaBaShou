/**
 * 需求服务
 * 对应后端 dabashou-demand 模块
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { Demand, DemandSearchParams, PublishDemandParams } from '../types/demand';

export const demandService = {
  /** 搜索需求列表 */
  search(params: DemandSearchParams) {
    return api.get<PageResult<Demand>>('/v1/demands', params as unknown as Record<string, unknown>);
  },

  /** 获取需求详情 */
  getDetail(demandId: number) {
    return api.get<Demand>(`/v1/demands/${demandId}`);
  },

  /** 发布需求 */
  publish(params: PublishDemandParams) {
    return api.post<{ id: number }>('/v1/demands', params as unknown as Record<string, unknown>);
  },

  /** 接单（揭榜） */
  bid(demandId: number) {
    return api.post<void>(`/v1/demands/${demandId}/bid`);
  },

  /** 关闭需求 */
  close(demandId: number) {
    return api.put<void>(`/v1/demands/${demandId}/close`);
  },

  /** 删除需求 */
  deleteDemand(demandId: number) {
    return api.delete<void>(`/v1/demands/${demandId}`);
  },

  /** 更新需求 */
  update(demandId: number, params: Partial<PublishDemandParams>) {
    return api.put<Demand>(`/v1/demands/${demandId}`, params as unknown as Record<string, unknown>);
  },

  /** 获取我的需求列表 */
  getMine(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Demand>>('/v1/demands/mine', params as unknown as Record<string, unknown>);
  },

  /** 匹配可接单的服务货架 */
  match(demandId: number, limit?: number) {
    return api.get<Array<{ shelfId: number; userId: number; nickname: string; avatar: string; title: string; pointPrice: number; trustScore: number; matchScore: number }>>(
      `/v1/demands/${demandId}/match`,
      limit ? { limit } as unknown as Record<string, unknown> : undefined
    );
  },
};
