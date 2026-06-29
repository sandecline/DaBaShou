import request from '@/utils/request'
import type { ApiResponse, LoginVo, UserProfileVo } from '@/types/api'

export function register(data: { username: string; password: string; nickname?: string; phone?: string }): Promise<ApiResponse<null>> {
  return request({ url: '/v1/auth/register', method: 'post', data })
}

export function login(data: { username: string; password: string }): Promise<ApiResponse<LoginVo>> {
  return request({ url: '/v1/auth/login', method: 'post', data })
}

export function sendSmsCode(phone: string): Promise<ApiResponse<null>> {
  return request({ url: '/v1/auth/sms-code', method: 'post', data: { phone } })
}

export function smsLogin(data: { phone: string; code: string }): Promise<ApiResponse<LoginVo>> {
  return request({ url: '/v1/auth/sms-login', method: 'post', data })
}

export function refreshToken(refreshToken: string): Promise<ApiResponse<LoginVo>> {
  return request({ url: '/v1/auth/refresh', method: 'post', data: { refreshToken } })
}

export function logout(): Promise<ApiResponse<null>> {
  return request({ url: '/v1/auth/logout', method: 'post' })
}

export function getProfile(): Promise<ApiResponse<UserProfileVo>> {
  return request({ url: '/v1/user/profile', method: 'get' })
}

export function updateProfile(data: { nickname?: string; avatar?: string; bio?: string; campus?: string; building?: string }): Promise<ApiResponse<null>> {
  return request({ url: '/v1/user/profile', method: 'put', data })
}

export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<null>> {
  return request({ url: '/v1/user/password', method: 'put', data })
}

export function updateLocation(data: { longitude: number; latitude: number }): Promise<ApiResponse<null>> {
  return request({ url: '/v1/user/location', method: 'put', data })
}

export function submitCampusAuth(data: { authType: string; studentNo: string; realName: string; campus: string; college?: string; credentialFileId?: number }): Promise<ApiResponse<null>> {
  return request({ url: '/v1/user/campus-auth', method: 'post', data })
}

export function getCampusAuth(): Promise<ApiResponse<any>> {
  return request({ url: '/v1/user/campus-auth', method: 'get' })
}

export function getTrustScore(): Promise<ApiResponse<any>> {
  return request({ url: '/v1/user/trust-score', method: 'get' })
}
