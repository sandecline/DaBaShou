/** 需求（悬赏求助） */
export interface Demand {
  id: number
  userId: number
  skillTagId: number | null
  title: string
  description: string
  pointReward: number
  deadline: string | null
  locationType: 1 | 2 | 3 // 1-线上 2-线下 3-均可
  longitude: number
  latitude: number
  campus: string
  building: string
  status: 0 | 1 | 2 | 3 // 0-已关闭 1-待接单 2-进行中 3-已完成
  createTime: string
  updateTime: string
  // 前端附加字段
  userName?: string
  userAvatar?: string
  tagName?: string
  categoryName?: string
  distance?: number
  isUrgent?: boolean // 是否急单（12h内截止）
}

/** 需求发布/编辑表单 */
export interface DemandForm {
  skillTagId: number | null
  title: string
  description: string
  pointReward: number
  deadline: string | null
  locationType: 1 | 2 | 3
  isUrgent: boolean
}
