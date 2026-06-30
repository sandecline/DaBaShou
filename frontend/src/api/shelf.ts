import request from '@/utils/request'
import type { ShelfItemVo, ShelfDetailVo, TimeSlotVo, PageResult, PageParams } from '@/types/api'

export function publishShelf(data: {
  skillTagId: number; title: string; description?: string;
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
  latitude?: number; pageNum?: number; pageSize?: number
}): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/shelves', params)
}

export function getMyShelves(params?: PageParams): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/shelves/mine', params)
}

export function getUserShelves(userId: number, params?: PageParams): Promise<PageResult<ShelfItemVo>> {
  return request.get('/v1/users/' + userId + '/shelves', params)
}

export function setTimeSlots(shelfId: number, slots: { dayOfWeek: number; startTime: string; endTime: string }[]): Promise<null> {
  return request.post('/v1/shelves/' + shelfId + '/timeslots', slots)
}

export function getTimeSlots(shelfId: number): Promise<TimeSlotVo[]> {
  return request.get('/v1/shelves/' + shelfId + '/timeslots')
}

export function deleteTimeSlot(shelfId: number, slotId: number): Promise<null> {
  return request.delete('/v1/shelves/' + shelfId + '/timeslots/' + slotId)
}