/** 时间格子 */
export interface TimeSlot {
  id: number
  userId: number
  date: string // YYYY-MM-DD
  startTime: string // HH:mm
  endTime: string // HH:mm
  status: 0 | 1 | 2 // 0-不可用 1-可预约 2-已预约
  createTime: string
  updateTime: string
}

/** 技能货架（发布的服务） */
export interface SkillShelf {
  id: number
  userId: number
  skillTagId: number
  title: string
  description: string
  pointPrice: number
  durationMinutes: number
  locationType: 1 | 2 | 3 // 1-线上 2-线下 3-均可
  status: 0 | 1 | 2 // 0-下架 1-上架 2-审核中
  createTime: string
  updateTime: string
  // 前端附加字段
  userName?: string
  userAvatar?: string
  coverImage?: string
  tagName?: string
  categoryName?: string
  trustScore?: number
  distance?: number // 距离（米）
  heatScore?: number // 热度分
}

/** 技能服务发布/编辑表单 */
export interface SkillShelfForm {
  skillTagId: number | null
  title: string
  description: string
  pointPrice: number
  durationMinutes: number
  locationType: 1 | 2 | 3
  timeSlots: TimeSlotInput[]
}

export interface TimeSlotInput {
  date: string
  startTime: string
  endTime: string
}
