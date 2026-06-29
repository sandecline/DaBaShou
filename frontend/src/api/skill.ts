import request from '@/utils/request'
import type { SkillCategory, SkillTag, UserSkill, PageResult, PageParams } from '@/types'

export function getCategories(): Promise<SkillCategory[]> {
  return request.get('/skill/categories')
}

export function getTags(categoryId?: number): Promise<SkillTag[]> {
  return request.get('/skill/tags', { categoryId })
}

export function getUserSkills(userId?: number): Promise<UserSkill[]> {
  return request.get('/user/skills', { userId })
}

export function addUserSkill(data: { skillTagId: number; proficiency: number; description?: string }): Promise<any> {
  return request.post('/user/skills', data)
}

export function deleteUserSkill(skillId: number): Promise<any> {
  return request.delete(`/user/skills/${skillId}`)
}

export function getAllSkills(): Promise<PageResult<SkillTag>> {
  return request.get('/skill/all')
}
