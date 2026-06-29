/** 统一API响应格式 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/** 分页请求参数 */
export interface PageParams {
  page?: number
  size?: number
  [key: string]: any
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 登录请求 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userInfo: import('./user').User
}

/** 注册请求 */
export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  phone?: string
  email?: string
}
