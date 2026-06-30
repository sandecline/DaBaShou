/**
 * 用户认证 & Token 管理
 */
import { post } from './request'

/**
 * 微信小程序登录
 * 后端需实现 /api/v1/auth/wechat-login 接口
 */
export async function wechatLogin() {
  const { code } = await uni.login()
  return post('/auth/wechat-login', { code }, { noAuth: true })
}

/**
 * 普通账号密码登录
 */
export function loginByPassword(username, password) {
  return post('/auth/login', { username, password }, { noAuth: true })
}

/**
 * 注册
 */
export function register(data) {
  return post('/auth/register', data, { noAuth: true })
}

/**
 * 保存 Token 到本地
 */
export function saveToken(token) {
  uni.setStorageSync('access_token', token)
}

/**
 * 清除 Token
 */
export function clearToken() {
  uni.removeStorageSync('access_token')
}

/**
 * 检查是否已登录（本地有无 Token）
 */
export function isLoggedIn() {
  return !!uni.getStorageSync('access_token')
}
