import request from '@/utils/request'
import type { LoginVo } from '@/types/api'

export function register(data: { username: string; password: string; nickname?: string; phone?: string }): Promise<null> {
  return request.post('/v1/auth/register', data)
}

export function login(data: { username: string; password: string }): Promise<LoginVo> {
  return request.post('/v1/auth/login', data)
}

export function sendSmsCode(phone: string): Promise<null> {
  return request.post('/v1/auth/sms-code', { phone })
}

export function smsLogin(data: { phone: string; code: string }): Promise<LoginVo> {
  return request.post('/v1/auth/sms-login', data)
}

export function refreshToken(refreshToken: string): Promise<LoginVo> {
  return request.post('/v1/auth/refresh', { refreshToken })
}

export function logout(): Promise<null> {
  return request.post('/v1/auth/logout')
}

