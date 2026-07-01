/**
 * 统一 API 响应格式
 * 与后端 CommonResult 保持一致（AGENTS.md 约束）
 */

export interface ApiResponse<T = unknown> {
  /** 状态码：200 成功，4xx/5xx 异常 */
  code: number;
  /** 提示信息 */
  msg: string;
  /** 响应数据 */
  data: T;
}

/** 分页请求参数 */
export interface PageParams {
  /** 当前页码（从 1 开始） */
  pageNum: number;
  /** 每页条数 */
  pageSize: number;
}

/** 分页响应结构 */
export interface PageResult<T> {
  /** 总记录数 */
  total: number;
  /** 当前页数据 */
  list: T[];
  /** 当前页码 */
  pageNum: number;
  /** 每页条数 */
  pageSize: number;
  /** 总页数 */
  pages: number;
}
