/**
 * 编辑资料页
 * 修改昵称、校区、楼栋、个人简介等信息
 */

import { userService } from '../../../services/user';
import type { UserProfile, UpdateProfileParams } from '../../../types/user';

/** 7 天（毫秒） */
const NICKNAME_COOLDOWN = 7 * 24 * 60 * 60 * 1000;

/** 常用楼栋选项 */
const BUILDING_OPTIONS = [
  '1号楼', '2号楼', '3号楼', '4号楼', '5号楼',
  '6号楼', '7号楼', '8号楼', '9号楼', '10号楼',
  '11号楼', '12号楼', '13号楼', '14号楼', '15号楼',
  '16号楼', '17号楼', '18号楼', '19号楼', '20号楼',
  '图书馆', '教学楼A', '教学楼B', '教学楼C',
];

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
    /** 昵称是否可编辑（7天冷却） */
    canEditNickname: true,
    /** 昵称冷却剩余天数 */
    nicknameDaysLeft: 0,
  },

  onLoad() {
    this.loadProfile();
    this.checkNicknameCooldown();
  },

  /** 检查昵称修改冷却 */
  checkNicknameCooldown() {
    const lastChange = wx.getStorageSync('last_nickname_change');
    if (lastChange) {
      const elapsed = Date.now() - lastChange;
      if (elapsed < NICKNAME_COOLDOWN) {
        const daysLeft = Math.ceil((NICKNAME_COOLDOWN - elapsed) / (24 * 60 * 60 * 1000));
        this.setData({ canEditNickname: false, nicknameDaysLeft: daysLeft });
      }
    }
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

  /** 选择楼栋 */
  onBuildingTap() {
    wx.showActionSheet({
      itemList: BUILDING_OPTIONS,
      success: (res) => {
        this.setData({ building: BUILDING_OPTIONS[res.tapIndex] });
      },
    });
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

      // 如果修改了昵称，记录冷却时间
      if (params.nickname) {
        wx.setStorageSync('last_nickname_change', Date.now());
        this.setData({ canEditNickname: false, nicknameDaysLeft: 7 });
      }

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
