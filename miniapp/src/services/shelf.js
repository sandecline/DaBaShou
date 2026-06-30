import { get, post, put, del } from '@/utils/request'

export function getCategories() { return get('/skills/categories/tree') }

export function getTags(categoryId) { return get('/skills/tags', { categoryId }) }

export function getShelfList(params) { return get('/shelves', params) }

export function getShelfDetail(id) { return get('/shelves/' + id) }

export function getMyShelves(params) { return get('/shelves/mine', params) }

export function publishShelf(data) { return post('/shelves', data) }

export function updateShelf(id, data) { return put('/shelves/' + id, data) }

export function shelfOn(id) { return put('/shelves/' + id + '/on') }

export function shelfOff(id) { return put('/shelves/' + id + '/off') }

export function deleteShelf(id) { return del('/shelves/' + id) }
