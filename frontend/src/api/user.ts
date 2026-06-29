import request from '@/utils/request'
import type { LoginParams, LoginResult, RegisterParams, ApiResponse } from '@/types'
import type { User, CampusAuth } from '@/types'

export function login(data: LoginParams): Promise<LoginResult> {
  return request.post('/user/login', data)
}

export function register(data: RegisterParams): Promise<ApiResponse> {
  return request.post('/user/register', data)
}

export function getProfile(): Promise<User> {
  return request.get('/user/profile')
}

export function updateProfile(data: Partial<User>): Promise<ApiResponse> {
  return request.put('/user/profile', data)
}

export function campusAuth(data: { studentId: string; studentName: string; campus: string; authType: string }): Promise<ApiResponse> {
  return request.post('/user/campus-auth', data)
}

export function getCampusAuthStatus(): Promise<CampusAuth> {
  return request.get('/user/campus-auth')
}

export function getUserById(userId: number): Promise<User> {
  return request.get(`/user/${userId}`)
}
