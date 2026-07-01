import request from '@/utils/request'
import type { ShelfItemVo, ShelfDetailVo, TimeSlotVo, PageResult, PageParams } from '@/types/api'
import { normalizePageParams } from './_params'

export function publishShelf(data: {
  skillTagId: number | null; title: string; description?: string;
  pointPrice: number; durationMinutes: number; locationType: number
}): Promise<number> {
  return request.post('/v1/shelves', data)
}

export function updateShelf(id: number, data: Partial<{
  skillTagId: number; title: string; description: string;
  pointPrice: number; durationMinutes: number; locationType: number
}>): Promise<null> {
  return request.put('/v1/shelves/' + id, data)
}

export function onShelf(id: number): Promise<null> {
  return request.put('/v1/shelves/' + id + '/on')
}

export function offShelf(id: number): Promise<null> {
  return request.put('/v1/shelves/' + id + '/off')
}

export function deleteShelf(id: number): Promise<null> {
  return request.delete('/v1/shelves/' + id)
}

export function getShelfDetail(id: number): Promise<ShelfDetailVo> {
  return request.get('/v1/shelves/' + id)
}

export function searchShelves(params: {
  keyword?: string; categoryId?: number; skillTagId?: number;
  locationType?: number; sortBy?: string; longitude?: number;
  latitude?: number; pageNum?: number; pageSize?: number; page?: number; size?: number; sort?: string
}): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/shelves', normalizePageParams(params))
}

export const getShelfList = searchShelves

export function getMyShelves(params?: PageParams): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/shelves/mine', normalizePageParams(params))
}

export function getUserShelves(userId: number, params?: PageParams): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/shelves/users/' + userId + '/shelves', normalizePageParams(params))
}

export function setTimeSlots(shelfId: number, slots: { date?: string; dayOfWeek: number; startTime: string; endTime: string }[]): Promise<null> {
  return request.post('/v1/shelves/' + shelfId + '/timeslots', slots)
}

export function getTimeSlots(shelfId: number): Promise<TimeSlotVo[]> {
  return request.get('/v1/shelves/' + shelfId + '/timeslots')
}

export function deleteTimeSlot(shelfId: number, slotId: number): Promise<null> {
  return request.delete('/v1/shelves/' + shelfId + '/timeslots/' + slotId)
}
