/**
 * 需求服务
 * 对应后端 dabashou-demand 模块
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { Demand, DemandDetailVo, DemandSearchParams, PublishDemandParams } from '../types/demand';

export const demandService = {
  /** 搜索需求列表 */
  search(params: DemandSearchParams) {
    return api.get<PageResult<Demand>>('/v1/demands', params as unknown as Record<string, unknown>);
  },

  /** 获取需求详情 */
  getDetail(demandId: number) {
    return api.get<DemandDetailVo>(`/v1/demands/${demandId}`);
  },

  /** 发布需求 */
  publish(params: PublishDemandParams) {
    return api.post<{ id: number }>('/v1/demands', params as unknown as Record<string, unknown>);
  },

  /** 接单（揭榜）。后端 POST /v1/demands/{id}/accept，返回 AcceptResultVo */
  accept(demandId: number, shelfId: number, idempotentToken: string, remark?: string) {
    return api.post<{ demandId: number; shelfId: number; buyerId: number; skillTagId: number; title: string; pointReward: number; idempotentToken: string; remark: string }>(`/v1/demands/${demandId}/accept`, {
      shelfId,
      idempotentToken,
      remark,
    } as unknown as Record<string, unknown>);
  },

  /** 匹配需求对应的服务货架 GET /v1/demands/{id}/match */
  match(demandId: number, limit = 10) {
    return api.get<Array<Record<string, unknown>>>(`/v1/demands/${demandId}/match`, { limit } as unknown as Record<string, unknown>);
  },

  /** 关闭/取消需求 */
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
};
