/** 技能分类 */
export interface SkillCategory {
  id: number
  name: string
  icon: string
  sortOrder: number
  status: 0 | 1
  createTime: string
}

/** 技能标签 */
export interface SkillTag {
  id: number
  categoryId: number
  name: string
  status: 0 | 1
  createTime: string
  // 前端附加字段
  categoryName?: string
}

/** 用户技能 */
export interface UserSkill {
  id: number
  userId: number
  skillTagId: number
  proficiency: Proficiency
  description: string
  createTime: string
  // 前端附加字段
  tagName?: string
  categoryName?: string
}

/** 熟练度 */
export type Proficiency = 1 | 2 | 3 | 4 // 1-了解 2-熟悉 3-精通 4-专家

export const ProficiencyMap: Record<Proficiency, string> = {
  1: '了解',
  2: '熟悉',
  3: '精通',
  4: '专家',
}
