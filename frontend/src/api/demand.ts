import request from '@/utils/request'
import type { ApiResponse, PageResult, DemandItemVo, DemandDto, DemandMatchVo } from '@/types/api'

export function publishDemand(data: DemandDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/demands', method: 'post', data })
}

export function updateDemand(id: number, data: DemandDto): Promise<ApiResponse<null>> {
  return request({ url: `/v1/demands/${id}`, method: 'put', data })
}

export function getDemandDetail(id: number): Promise<ApiResponse<DemandItemVo>> {
  return request({ url: `/v1/demands/${id}`, method: 'get' })
}

export function searchDemands(params?: {
  keyword?: string
  categoryId?: number
  skillTagId?: number
  demandType?: number
  status?: number
  sortBy?: 'time' | 'budget' | 'hot'
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<DemandItemVo>>> {
  return request({ url: '/v1/demands', method: 'get', params })
}

export function getMyDemands(params?: { pageNum?: number; pageSize?: number }): Promise<ApiResponse<PageResult<DemandItemVo>>> {
  return request({ url: '/v1/demands/mine', method: 'get', params })
}

export function closeDemand(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/demands/${id}/close`, method: 'put' })
}

export function deleteDemand(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/demands/${id}`, method: 'delete' })
}

export function bidDemand(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/demands/${id}/bid`, method: 'post' })
}

export function getMatchRecommendations(id: number, limit = 10): Promise<ApiResponse<DemandMatchVo[]>> {
  return request({ url: `/v1/demands/${id}/match`, method: 'get', params: { limit } })
}
