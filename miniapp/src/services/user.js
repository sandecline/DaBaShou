import { get, put } from '@/utils/request'

export function getProfile() { return get('/user/profile') }

export function updateProfile(data) { return put('/user/profile', data) }

export function getUserSkills() { return get('/users/me/skills') }

export function addUserSkill(data) { return post('/users/me/skills', data) }

export function updateUserSkill(id, data) { return put('/users/me/skills/' + id, data) }

export function deleteUserSkill(id) { return del('/users/me/skills/' + id) }

export function getUserPoints() { return get('/points/balance') }

export function getPointTransactions(params) { return get('/points/transactions', params) }
