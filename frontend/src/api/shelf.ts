import request from '@/utils/request'
import type { ApiResponse, PageResult, ShelfItemVo, ShelfDetailVo, SkillShelfDto, TimeSlotVo, TimeSlotDto, ShelfSearchParams } from '@/types/api'
import type { SkillShelf } from '@/types'

export function publishShelf(data: SkillShelfDto): Promise<ApiResponse<number>> {
  return request({ url: '/v1/shelves', method: 'post', data })
}

export function updateShelf(id: number, data: SkillShelfDto): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${id}`, method: 'put', data })
}

export function getShelfDetail(id: number): Promise<ApiResponse<ShelfDetailVo>> {
  return request({ url: `/v1/shelves/${id}`, method: 'get' })
}

export function searchShelves(params: ShelfSearchParams): Promise<ApiResponse<PageResult<ShelfItemVo>>> {
  return request({ url: '/v1/shelves', method: 'get', params })
}

export function getShelfList(params: any): Promise<ApiResponse<any>> {
  return request({ url: '/v1/shelves', method: 'get', params })
}

export function getMyShelves(params?: { pageNum?: number; pageSize?: number }): Promise<ApiResponse<PageResult<ShelfItemVo>>> {
  return request({ url: '/v1/shelves/mine', method: 'get', params })
}

export function getUserShelves(userId: number, params?: { pageNum?: number; pageSize?: number }): Promise<ApiResponse<PageResult<ShelfItemVo>>> {
  return request({ url: `/v1/users/${userId}/shelves`, method: 'get', params })
}

export function onShelf(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${id}/on`, method: 'put' })
}

export function offShelf(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${id}/off`, method: 'put' })
}

export function deleteShelf(id: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${id}`, method: 'delete' })
}

export function setTimeSlots(shelfId: number, data: TimeSlotDto[]): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${shelfId}/timeslots`, method: 'post', data })
}

export function getTimeSlots(shelfId: number): Promise<ApiResponse<TimeSlotVo[]>> {
  return request({ url: `/v1/shelves/${shelfId}/timeslots`, method: 'get' })
}

export function deleteTimeSlot(shelfId: number, slotId: number): Promise<ApiResponse<null>> {
  return request({ url: `/v1/shelves/${shelfId}/timeslots/${slotId}`, method: 'delete' })
}

export function publishSkill(data: any): Promise<ApiResponse<number>> {
  return request({ url: '/v1/shelves', method: 'post', data })
}

export function getMyShelfList(params?: any): Promise<ApiResponse<any>> {
  return request({ url: '/v1/shelves/mine', method: 'get', params })
}

// Shim: createOrder used in skill/detail.vue
export function createOrder(data?: any): any {
  return request({ url: '/v1/orders/from-shelf', method: 'post', data })
}

