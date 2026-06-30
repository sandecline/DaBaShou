import request from '@/utils/request'
import type { ApiResponse, SkillCategoryVo, SkillTagVo, UserSkillVo, UserSkillDto } from '@/types/api'

export function getCategoryTree(): Promise<ApiResponse<SkillCategoryVo[]>> {
  return request({ url: '/v1/skills/categories/tree', method: 'get' })
}

export function getTags(categoryId?: number): Promise<ApiResponse<SkillTagVo[]>> {
  return request({ url: '/v1/skills/tags', method: 'get', params: { categoryId } })
}

export function getCategories(): Promise<ApiResponse<SkillCategoryVo[]>> {
  return request({ url: '/v1/skills/categories/tree', method: 'get' })
}

export function getHotTags(limit = 20): Promise<ApiResponse<SkillTagVo[]>> {
  return request({ url: '/v1/skills/tags/hot', method: 'get', params: { limit } })
}

export function getMySkills(): Promise<ApiResponse<UserSkillVo[]>> {
  return request({ url: '/v1/users/me/skills', method: 'get' })
}

export function addSkill(data: UserSkillDto): Promise<ApiResponse<UserSkillVo>> {
  return request({ url: '/v1/users/me/skills', method: 'post', data })
}

export function updateSkill(id: number, data: UserSkillDto): Promise<ApiResponse<null>> {
  return request({ url: `/v1/users/me/skills/${id}`, method: 'put', data })
}

export function removeSkill(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/users/me/skills/${id}`, method: 'delete' })
}
