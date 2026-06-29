import request from '@/utils/request'
import type { SkillShelf, SkillShelfForm, PageResult, PageParams } from '@/types'

export function publishSkill(data: SkillShelfForm): Promise<any> {
  return request.post('/shelf', data)
}

export function getShelfList(params: PageParams): Promise<PageResult<SkillShelf>> {
  return request.get('/shelf/list', params)
}

export function getShelfDetail(id: number): Promise<SkillShelf> {
  return request.get(`/shelf/${id}`)
}

export function updateShelf(id: number, data: Partial<SkillShelfForm>): Promise<any> {
  return request.put(`/shelf/${id}`, data)
}

export function deleteShelf(id: number): Promise<any> {
  return request.delete(`/shelf/${id}`)
}

export function updateShelfStatus(id: number, status: 0 | 1): Promise<any> {
  return request.put(`/shelf/${id}/status`, { status })
}

export function getMyShelfList(params: PageParams): Promise<PageResult<SkillShelf>> {
  return request.get('/shelf/my', params)
}
