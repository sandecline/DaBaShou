import request from '@/utils/request'
import type { ApiResponse, PageResult, FileVo, RoleVo, PermissionVo, LogVo, ConfigVo } from '@/types/api'

export function uploadFile(file: File): Promise<ApiResponse<FileVo>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/v1/files/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function downloadFile(id: number): Promise<Blob> {
  return request({ url: `/v1/files/${id}/download`, method: 'get', responseType: 'blob' })
}

export function getRoles(): Promise<ApiResponse<RoleVo[]>> {
  return request({ url: '/admin/v1/roles', method: 'get' })
}

export function createRole(data: Partial<RoleVo>): Promise<ApiResponse<number>> {
  return request({ url: '/admin/v1/roles', method: 'post', data })
}

export function updateRole(id: number, data: Partial<RoleVo>): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/roles/${id}`, method: 'put', data })
}

export function deleteRole(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/roles/${id}`, method: 'delete' })
}

export function getPermissionTree(): Promise<ApiResponse<PermissionVo[]>> {
  return request({ url: '/admin/v1/permissions/tree', method: 'get' })
}

export function assignPermissions(roleId: number, permissionIds: number[]): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/roles/${roleId}/permissions`, method: 'post', data: { permissionIds } })
}

export function assignRoles(userId: number, roleIds: number[]): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/users/${userId}/roles`, method: 'post', data: { roleIds } })
}

export function getLogs(params?: {
  userId?: number
  module?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<LogVo>>> {
  return request({ url: '/admin/v1/logs', method: 'get', params })
}

export function getConfigs(params?: { pageNum?: number; pageSize?: number }): Promise<ApiResponse<PageResult<ConfigVo>>> {
  return request({ url: '/admin/v1/configs', method: 'get', params })
}

export function updateConfig(key: string, value: string): Promise<ApiResponse<null>> {
  return request({ url: `/admin/v1/configs/${key}`, method: 'put', data: { configValue: value } })
}
