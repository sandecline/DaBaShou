import request from '@/utils/request'
import type { DemandItemVo, DemandDetailVo, DemandMatchVo, PageResult, PageParams } from '@/types/api'

export function publishDemand(data: {
  skillTagId: number; title: string; description?: string;
  pointReward?: number; deadline?: string; locationType: number;
  demandType?: number; longitude?: number; latitude?: number
}): Promise<number> {
  return request.post('/v1/demands', data)
}

export function updateDemand(id: number, data: Partial<{
  skillTagId: number; title: string; description: string;
  pointReward: number; deadline: string; locationType: number
}>): Promise<null> {
  return request.put('/v1/demands/' + id, data)
}

export function closeDemand(id: number): Promise<null> {
  return request.put('/v1/demands/' + id + '/close')
}

export function deleteDemand(id: number): Promise<null> {
  return request.delete('/v1/demands/' + id)
}

export function getDemandDetail(id: number): Promise<DemandDetailVo> {
  return request.get('/v1/demands/' + id)
}

export function searchDemands(params: {
  keyword?: string; categoryId?: number; skillTagId?: number;
  demandType?: number; status?: number; sortBy?: string;
  pageNum?: number; pageSize?: number
}): Promise<PageResult<DemandItemVo>> {
  return request.get('/v1/demands', params)
}

export function getMyDemands(params?: PageParams): Promise<PageResult<DemandItemVo>> {
  return request.get('/v1/demands/mine', params)
}

export function bidDemand(id: number): Promise<null> {
  return request.post('/v1/demands/' + id + '/bid')
}

export function matchDemands(id: number, limit?: number): Promise<DemandMatchVo[]> {
  return request.get('/v1/demands/' + id + '/match', { limit })
}