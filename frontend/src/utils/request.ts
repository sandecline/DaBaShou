import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, logout } from './auth'
import type { ApiResponse } from '@/types'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const url = String(config.url || '')
    const isAuthEndpoint = url.includes('/v1/auth/')
    const token = isAuthEndpoint ? null : getToken()
    ;(config as any).__hadAuthToken = !!token
    ;(config as any).__isAuthEndpoint = isAuthEndpoint
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

function redirectToLogin() {
  const current = window.location.pathname + window.location.search
  if (window.location.pathname !== '/login') {
    window.location.replace(`/login?redirect=${encodeURIComponent(current)}`)
  }
}

function handleUnauthorized(hadAuthToken: boolean, msg?: string) {
  if (hadAuthToken) {
    ElMessage.error('登录已过期，请重新登录')
    logout()
    redirectToLogin()
    return
  }
  ElMessage.error(msg || '请先登录')
}

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, msg, data } = response.data

    if (code === 200) {
      return data as any
    }

    // token过期
    if (code === 401) {
      const config = response.config as any
      handleUnauthorized(!!config.__hadAuthToken && !config.__isAuthEndpoint, msg)
      return Promise.reject(new Error(msg))
    }

    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error) => {
    if (error.response?.status === 401) {
      const config = error.config as any
      handleUnauthorized(!!config?.__hadAuthToken && !config?.__isAuthEndpoint, error.response?.data?.msg)
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问')
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器异常，请稍后重试')
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  },
)

const request = {
  get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, { params, ...config })
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config)
  },
}

export default request
