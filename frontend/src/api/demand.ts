import request from '@/utils/request'
import type { Demand, DemandForm, PageResult, PageParams } from '@/types'

export function publishDemand(data: DemandForm): Promise<any> {
  return request.post('/demand', data)
}

export function getDemandList(params: PageParams): Promise<PageResult<Demand>> {
  return request.get('/demand/list', params)
}

export function getDemandDetail(id: number): Promise<Demand> {
  return request.get(`/demand/${id}`)
}

export function updateDemand(id: number, data: Partial<DemandForm>): Promise<any> {
  return request.put(`/demand/${id}`, data)
}

export function deleteDemand(id: number): Promise<any> {
  return request.delete(`/demand/${id}`)
}

export function closeDemand(id: number): Promise<any> {
  return request.put(`/demand/${id}/close`)
}

export function getMyDemandList(params: PageParams): Promise<PageResult<Demand>> {
  return request.get('/demand/my', params)
}
