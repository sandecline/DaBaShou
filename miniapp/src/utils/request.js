/**
 * 统一网络请求封装
 * 基于 uni.request，自动处理 Token、401 跳登录、统一错误提示
 */
import { useUserStore } from '@/store/user'

// #ifdef H5
const BASE_URL = '/api/v1'
// #endif
// #ifdef MP-WEIXIN
const BASE_URL = 'http://localhost:9090/api/v1'   // 开发环境
// const BASE_URL = 'https://api.dabashou.com/api/v1'  // 生产环境
// #endif

/** 获取当前 Token */
function getToken() {
  // #ifdef H5
  return uni.getStorageSync('access_token')
  // #endif
  // #ifdef MP-WEIXIN
  return wx.getStorageSync('access_token')
  // #endif
}

/**
 * 通用请求方法
 * @param {string} options.url    接口路径（不含 BASE_URL）
 * @param {string} options.method 请求方法
 * @param {object} options.data   请求参数
 * @param {object} options.header 自定义头
 * @param {boolean} options.noAuth 跳过 Token
 * @param {boolean} options.noToast 不显示错误提示
 */
export function request(options) {
  return new Promise((resolve, reject) => {
    const token = options.noAuth ? '' : getToken()
    const header = {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: 'Bearer ' + token } : {}),
      ...options.header,
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        const body = res.data
        if (body.code === 200 || body.code === undefined) {
          resolve(body.data !== undefined ? body.data : body)
        } else if (body.code === 401) {
          // Token 过期，跳转登录页
          const userStore = useUserStore()
          userStore.logout()
          uni.reLaunch({ url: '/pages/auth/auth' })
          reject(new Error(body.msg || '登录已过期'))
        } else {
          if (!options.noToast) {
            uni.showToast({ title: body.msg || '请求失败', icon: 'none' })
          }
          reject({ code: body.code, msg: body.msg })
        }
      },
      fail: (err) => {
        if (!options.noToast) {
          uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        }
        reject({ code: -1, msg: '网络异常', detail: err })
      },
    })
  })
}

// ========== 便捷方法 ==========

export function get(url, data, options) {
  return request({ url, method: 'GET', data, ...options })
}

export function post(url, data, options) {
  return request({ url, method: 'POST', data, ...options })
}

export function put(url, data, options) {
  return request({ url, method: 'PUT', data, ...options })
}

export function del(url, data, options) {
  return request({ url, method: 'DELETE', data, ...options })
}
