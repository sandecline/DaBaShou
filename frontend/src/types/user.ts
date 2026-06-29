/** 用户信息 */
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  phone: string
  email: string
  pointBalance: number
  trustScore: number
  longitude: number
  latitude: number
  campus: string
  building: string
  bio: string
  status: 0 | 1
  createTime: string
  updateTime: string
}

/** 校园认证 */
export interface CampusAuth {
  id: number
  userId: number
  studentId: string
  studentName: string
  campus: string
  authType: 'student_id' | 'campus_email' | 'student_card'
  status: 0 | 1 | 2 // 0-未认证 1-审核中 2-已认证
  createTime: string
}

/** 信任分变动记录 */
export interface TrustScoreLog {
  id: number
  userId: number
  changeAmount: number
  reason: string
  beforeScore: number
  afterScore: number
  createTime: string
}

/** 信任分等级 */
export type TrustLevel = 'newcomer' | 'reliable' | 'gold'

/** 用户角色 */
export type UserRole = 'student' | 'admin' | 'support'
