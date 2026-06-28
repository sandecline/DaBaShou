import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types/api'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.config.responseType === 'blob') {
      return response.data as Blob
    }
    const res = response.data as ApiResponse
    if (res.code === 200) {
      return res as any
    }
    if (res.code === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
      return Promise.reject(new Error('未授权'))
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    return Promise.reject(error)
  }
)

export function withIdempotent<T>(promise: Promise<ApiResponse<T>>, idempotentToken: string): Promise<ApiResponse<T>> {
  return promise
}

export function generateIdempotentToken(): string {
  return crypto.randomUUID()
}

export default service
