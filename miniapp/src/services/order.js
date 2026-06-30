import { get, post, put } from '@/utils/request'

export function getOrderList(params) { return get('/orders', params) }

export function getOrderDetail(id) { return get('/orders/' + id + '/detail') }

export function getOrderStatus(id) { return get('/orders/' + id + '/status') }

export function createOrderFromShelf(data) { return post('/orders/from-shelf', data) }

export function payOrder(id, token) { return post('/orders/' + id + '/pay', {}, { params: { idempotentToken: token } }) }

export function cancelOrder(id, reason) { return put('/orders/' + id + '/cancel', { reason }) }

export function startService(id) { return put('/orders/' + id + '/start') }

export function verifyOrder(id, code) { return put('/orders/' + id + '/verify', { code }) }

export function confirmOrder(id) { return put('/orders/' + id + '/confirm') }

export function requestRefund(id, reason) { return post('/orders/' + id + '/refund', { reason }) }
