import request from '@/utils/request'
import type { CategoryTreeNode, SkillTagVo, UserSkillVo } from '@/types/api'

export function getCategoryTree(): Promise<CategoryTreeNode[]> {
  return request.get('/v1/skills/categories/tree')
}

export function getTags(categoryId?: number): Promise<SkillTagVo[]> {
  return request.get('/v1/skills/tags', { categoryId })
}

export function getHotTags(limit?: number): Promise<SkillTagVo[]> {
  return request.get('/v1/skills/tags/hot', { limit })
}

export function getMySkills(): Promise<UserSkillVo[]> {
  return request.get('/v1/users/me/skills')
}

export function addMySkill(data: { skillTagId: number; proficiency: number; description?: string }): Promise<{ id: number }> {
  return request.post('/v1/users/me/skills', data)
}

export function updateMySkill(id: number, data: { proficiency?: number; description?: string }): Promise<null> {
  return request.put('/v1/users/me/skills/' + id, data)
}

export function deleteMySkill(id: number): Promise<null> {
  return request.delete('/v1/users/me/skills/' + id)
}