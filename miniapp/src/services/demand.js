import { get, post, put, del } from '@/utils/request'

export function getDemandList(params) { return get('/demands', params) }

export function getDemandDetail(id) { return get('/demands/' + id) }

export function getMyDemands(params) { return get('/demands/mine', params) }

export function publishDemand(data) { return post('/demands', data) }

export function updateDemand(id, data) { return put('/demands/' + id, data) }

export function closeDemand(id) { return put('/demands/' + id + '/close') }

export function deleteDemand(id) { return del('/demands/' + id) }

export function acceptDemand(id, data) { return post('/demands/' + id + '/accept', data) }
