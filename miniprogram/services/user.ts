/**
 * 用户服务
 * 对应后端 dabashou-user 模块
 * 与 frontend/src/api/auth.ts 对齐
 */

import { api } from '../utils/request';
import type { UserProfile } from '../types/user';

export const userService = {
  /** 获取我的个人信息 */
  getProfile() {
    return api.get<UserProfile>('/v1/user/profile');
  },

  /** 编辑个人信息 */
  updateProfile(params: {
    nickname?: string;
    avatar?: string;
    bio?: string;
    campus?: string;
    building?: string;
  }) {
    return api.put<UserProfile>('/v1/user/profile', params as unknown as Record<string, unknown>);
  },

  /** 修改密码 */
  changePassword(oldPassword: string, newPassword: string) {
    return api.put<void>('/v1/user/password', { oldPassword, newPassword });
  },

  /** 更新位置 */
  updateLocation(longitude: number, latitude: number) {
    return api.put<void>('/v1/user/location', { longitude, latitude });
  },

  /** 获取他人公开信息 */
  getUserPublic(userId: number) {
    return api.get<UserProfile>(`/v1/users/${userId}`);
  },

  /** 上传头像（wx.uploadFile） */
  uploadAvatar(filePath: string) {
    const token = wx.getStorageSync('access_token');
    return new Promise<{ url: string }>((resolve, reject) => {
      wx.uploadFile({
        url: '/v1/user/avatar',
        filePath,
        name: 'file',
        header: { Authorization: `Bearer ${token}` },
        success(res) {
          const data = JSON.parse(res.data);
          if (data.code === 200) resolve(data.data);
          else reject(data);
        },
        fail: reject,
      });
    });
  },

  /** 提交校园认证 */
  submitCampusAuth(params: {
    authType: 'student_id' | 'campus_email' | 'student_card';
    studentNo: string;
    realName: string;
    campus: string;
    college?: string;
    credentialFileId?: string;
  }) {
    return api.post<Record<string, unknown>>('/v1/user/campus-auth', params as unknown as Record<string, unknown>);
  },

  /** 获取校园认证状态 */
  getCampusAuth() {
    return api.get<Record<string, unknown>>('/v1/user/campus-auth');
  },

  /** 获取信任分详情 */
  getTrustScore() {
    return api.get<{ score: number; level: string; recentLogs: Array<Record<string, unknown>> }>('/v1/user/trust-score');
  },
};
