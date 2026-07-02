import request from '@/utils/request'
import type { CampusAuthVo, PublicUserVo, TrustScoreVo, UserProfileVo } from '@/types/api'

export function getProfile(): Promise<UserProfileVo> {
  return request.get('/v1/user/profile')
}

export function updateProfile(data: { nickname?: string; avatar?: string; bio?: string; campus?: string; building?: string }): Promise<null> {
  return request.put('/v1/user/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<null> {
  return request.put('/v1/user/password', data)
}

export function updateLocation(data: { longitude: number; latitude: number }): Promise<null> {
  return request.put('/v1/user/location', data)
}

export function submitCampusAuth(data: { authType: string; studentNo: string; realName: string; campus: string; college?: string; credentialFileId?: number }): Promise<null> {
  return request.post('/v1/user/campus-auth', data)
}

export function getCampusAuth(): Promise<CampusAuthVo> {
  return request.get('/v1/user/campus-auth')
}

export function getTrustScore(): Promise<TrustScoreVo> {
  return request.get('/v1/user/trust-score')
}

export function getUserById(userId: number): Promise<PublicUserVo> {
  return request.get('/v1/users/' + userId)
}
