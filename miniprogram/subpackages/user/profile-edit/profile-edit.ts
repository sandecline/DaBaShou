/**
 * 编辑资料页
 * 修改昵称、校区、楼栋、个人简介等信息
 */

import { userService } from '../../../services/user';
import type { UserProfile, UpdateProfileParams } from '../../../types/user';

Page({
  data: {
    /** 当前用户头像 */
    avatar: '',
    /** 昵称 */
    nickname: '',
    /** 校区 */
    campus: '',
    /** 楼栋 */
    building: '',
    /** 个人简介 */
    bio: '',
    /** 原始数据（用于对比是否修改） */
    originalProfile: null as UserProfile | null,
    /** 提交中 */
    submitting: false,
  },

  onLoad() {
    this.loadProfile();
  },

  /** 加载当前用户信息 */
  loadProfile() {
    const app = getApp();
    const userInfo = app.globalData.userInfo as UserProfile | null;

    if (userInfo) {
      this.setData({
        avatar: userInfo.avatar || '',
        nickname: userInfo.nickname || '',
        campus: userInfo.campus || '',
        building: userInfo.building || '',
        bio: userInfo.bio || '',
        originalProfile: userInfo,
      });
    } else {
      this.fetchProfile();
    }
  },

  /** 从后端获取个人信息 */
  async fetchProfile() {
    try {
      wx.showLoading({ title: '加载中...', mask: true });
      const res = await userService.getProfile();
      const data = (res.data || res) as UserProfile;
      this.setData({
        avatar: data.avatar || '',
        nickname: data.nickname || '',
        campus: data.campus || '',
        building: data.building || '',
        bio: data.bio || '',
        originalProfile: data,
      });
    } catch (err) {
      console.error('[ProfileEdit] 获取用户信息失败:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  /** 选择头像 */
  onChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFiles[0].tempFilePath;
        this.uploadAvatar(filePath);
      },
    });
  },

  /** 上传头像 */
  async uploadAvatar(filePath: string) {
    try {
      wx.showLoading({ title: '上传中...', mask: true });
      const result = await userService.uploadAvatar(filePath);
      const data = (result as unknown as { url: string });
      this.setData({ avatar: data.url });
      wx.showToast({ title: '头像已更新', icon: 'success' });
    } catch (err) {
      console.error('[ProfileEdit] 头像上传失败:', err);
      wx.showToast({ title: '上传失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  /** 输入昵称 */
  onNicknameInput(e: WechatMiniprogram.Input) {
    this.setData({ nickname: e.detail.value });
  },

  /** 选择校区（#138 修复：统一为 onCampusTap） */
  onCampusTap() {
    wx.showActionSheet({
      itemList: ['清水河校区', '沙河校区', '九里堤校区'],
      success: (res) => {
        this.setData({ campus: res.tapIndex >= 0 ? ['清水河校区', '沙河校区', '九里堤校区'][res.tapIndex] : '' });
      },
    });
  },

  /** 输入楼栋 */
  onBuildingInput(e: WechatMiniprogram.Input) {
    this.setData({ building: e.detail.value });
  },

  /** 输入简介 */
  onBioInput(e: WechatMiniprogram.TextareaInput) {
    this.setData({ bio: e.detail.value });
  },

  /** 提交保存 */
  async onSubmit() {
    const { nickname, campus, building, bio, originalProfile, submitting } = this.data;
    if (submitting) return;

    // 构建更新参数，仅提交有变化的字段
    const params: UpdateProfileParams = {};

    if (nickname !== originalProfile?.nickname) params.nickname = nickname;
    if (campus !== originalProfile?.campus) params.campus = campus;
    if (building !== originalProfile?.building) params.building = building;
    if (bio !== originalProfile?.bio) params.bio = bio;

    if (Object.keys(params).length === 0) {
      wx.showToast({ title: '没有修改', icon: 'none' });
      return;
    }

    try {
      this.setData({ submitting: true });
      wx.showLoading({ title: '保存中...', mask: true });

      const res = await userService.updateProfile(params);
      const updatedProfile = (res.data || res) as UserProfile;

      // 更新全局状态
      const app = getApp();
      app.globalData.userInfo = updatedProfile;
      wx.setStorageSync('user_info', updatedProfile);

      // 更新本地 original
      this.setData({ originalProfile: updatedProfile });

      wx.hideLoading();
      wx.showToast({ title: '保存成功', icon: 'success' });

      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('[ProfileEdit] 保存失败:', err);
      wx.hideLoading();
      wx.showToast({ title: '保存失败，请重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },
});
