/**
 * 发布需求表单页
 * 填写求助信息并发布需求悬赏
 */

import { demandService } from '../../services/demand';
import { skillService } from '../../services/skill';
import { ensureLogin } from '../../utils/auth';
import { isTitleValid, isDescValid, isPointPrice } from '../../utils/validator';
import type { SkillCategory, SkillTag, LocationType } from '../../types/skill';
import type { PublishDemandParams, DemandType } from '../../types/demand';

/** 位置类型选项 */
const LOCATION_OPTIONS = [
  { label: '线上', value: 1 },
  { label: '线下', value: 2 },
  { label: '均可', value: 3 },
];

/** 需求类型选项 */
const DEMAND_TYPE_OPTIONS = [
  { label: '求助悬赏', value: 1, desc: '付出积分，寻找帮手' },
  { label: '技能置换', value: 2, desc: '用自己的技能交换帮助' },
];

Page({
  data: {
    // 分类与标签
    categories: [] as SkillCategory[],
    tags: [] as SkillTag[],
    categoryIndex: 0,
    tagIndex: 0,

    // 表单字段
    title: '',
    description: '',
    pointReward: '',
    deadline: '',
    deadlineDisplay: '请选择截止日期',
    /** 最小可选日期（今天），供 picker start 属性使用 */
    today: '',
    demandType: 1 as DemandType,
    demandTypeOptions: DEMAND_TYPE_OPTIONS,
    locationType: 1 as LocationType,
    locationOptions: LOCATION_OPTIONS,
    /** 是否急单（12h内） */
    isUrgent: false,

    // 图片
    images: [] as string[],
    maxImages: 9,

    // 提交状态
    submitting: false,

    // 字符计数
    titleLength: 0,
    descLength: 0,
  },

  async onLoad() {
    // #102 修复：检查登录状态
    const loggedIn = await ensureLogin();
    if (!loggedIn) {
      wx.showToast({ title: '请先登录', icon: 'error' });
      setTimeout(() => wx.navigateBack(), 1000);
      return;
    }
    this.loadCategories();
    // 设置默认截止日期为 7 天后
    this.setDefaultDeadline();
  },

  setDefaultDeadline() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const todayStr = `${year}-${month}-${day}`;

    const date = new Date();
    date.setDate(date.getDate() + 7);
    const dYear = date.getFullYear();
    const dMonth = String(date.getMonth() + 1).padStart(2, '0');
    const dDay = String(date.getDate()).padStart(2, '0');
    this.setData({
      today: todayStr,                      // #106 修复
      deadline: `${dYear}-${dMonth}-${dDay}`,
      deadlineDisplay: `${dYear}年${dMonth}月${dDay}日`,
    });
  },

  // ===== 数据加载 =====

  async loadCategories() {
    try {
      const res = await skillService.getCategories();
      const categories = res.data || [];
      this.setData({
        categories,
        tags: categories[0]?.tags || [],
      });
    } catch (err) {
      console.error('加载分类失败:', err);
      this.setData({
        categories: [{ id: 0, name: '加载失败', icon: '', sortOrder: 0 }],
        tags: [],
      });
    }
  },

  // ===== 分类选择 =====

  onCategoryChange(e: WechatMiniprogram.PickerChange) {
    const categoryIndex = Number(e.detail.value);
    const category = this.data.categories[categoryIndex];
    this.setData({
      categoryIndex,
      tags: category?.tags || [],
      tagIndex: 0,
    });
  },

  onTagChange(e: WechatMiniprogram.PickerChange) {
    this.setData({ tagIndex: Number(e.detail.value) });
  },

  // ===== 日期选择 =====

  onDeadlineChange(e: WechatMiniprogram.PickerChange) {
    const value = e.detail.value as string;
    // WeChat picker date returns "YYYY-MM-DD" format
    const [year, month, day] = value.split('-');
    this.setData({
      deadline: value,
      deadlineDisplay: `${year}年${month}月${day}日`,
    });
  },

  // ===== 需求类型选择 =====

  onDemandTypeChange(e: WechatMiniprogram.CustomEvent) {
    this.setData({ demandType: Number(e.detail.value) as DemandType });
  },

  // ===== 位置选择 =====

  onLocationChange(e: WechatMiniprogram.CustomEvent) {
    this.setData({ locationType: Number(e.detail.value) as LocationType });
  },

  onUrgentChange(e: WechatMiniprogram.CustomEvent) {
    this.setData({ isUrgent: e.detail.value });
  },

  // ===== 表单输入 =====

  onTitleInput(e: WechatMiniprogram.Input) {
    this.setData({ title: e.detail.value, titleLength: e.detail.value.length });
  },

  onDescInput(e: WechatMiniprogram.TextareaInput) {
    this.setData({ description: e.detail.value, descLength: e.detail.value.length });
  },

  onRewardInput(e: WechatMiniprogram.Input) {
    this.setData({ pointReward: e.detail.value });
  },

  // ===== 图片上传 =====

  onChooseImage() {
    const { images, maxImages } = this.data;
    const remaining = maxImages - images.length;
    if (remaining <= 0) {
      wx.showToast({ title: `最多上传${maxImages}张图片`, icon: 'none' });
      return;
    }

    wx.chooseImage({
      count: remaining,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ images: [...images, ...res.tempFilePaths] });
      },
    });
  },

  onRemoveImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const images = [...this.data.images];
    images.splice(index, 1);
    this.setData({ images });
  },

  onPreviewImage(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index],
    });
  },

  // ===== 表单校验 =====

  validate(): string | null {
    const { title, description, pointReward, deadline, tags, tagIndex } = this.data;

    if (!isTitleValid(title)) {
      return '标题需 2-50 个字';
    }
    if (!isDescValid(description)) {
      return '描述需 5-500 个字';
    }
    const reward = Number(pointReward);
    if (!isPointPrice(reward)) {
      return '悬赏积分需在 1-9999 之间';
    }
    if (!deadline) {
      return '请选择截止日期';
    }
    const deadlineDate = new Date(deadline);
    if (deadlineDate <= new Date()) {
      return '截止日期必须晚于今天';
    }
    if (!tags.length || !tags[tagIndex]) {
      return '请选择分类标签';
    }
    return null;
  },

  // ===== 提交发布 =====

  async onSubmit() {
    if (this.data.submitting) return;

    const error = this.validate();
    if (error) {
      wx.showToast({ title: error, icon: 'none' });
      return;
    }

    // #102 修复：再次确认登录态
    if (!getApp().globalData.token) {
      wx.showToast({ title: '登录已过期，请重新进入', icon: 'error' });
      return;
    }

    const { tagIndex, tags, title, description, pointReward, deadline, demandType, locationType, images, isUrgent } = this.data;

    wx.showLoading({ title: '发布中...', mask: true });
    this.setData({ submitting: true });

    try {
      // #103 修复：先将本地图片上传到服务器获取远程 URL
      let remoteImages: string[] = [];
      if (images && images.length > 0) {
        remoteImages = await this.uploadImages(images);
      }

      const params: PublishDemandParams = {
        skillTagId: tags[tagIndex].id,
        title: title.trim(),
        description: description.trim(),
        pointReward: Number(pointReward),
        deadline: `${deadline}T23:59:59`,
        demandType,
        locationType,
        images: remoteImages,
        isUrgent,
      };

      const res = await demandService.publish(params);
      wx.hideLoading();

      wx.showToast({ title: '发布成功', icon: 'success' });
      setTimeout(() => {
        wx.redirectTo({ url: `/pages/demand-detail/demand-detail?id=${res.data.id}` });
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      console.error('发布需求失败:', err);
      wx.showToast({ title: '发布失败，请重试', icon: 'error' });
    } finally {
      this.setData({ submitting: false });
    }
  },

  /**
   * 上传图片到服务器，返回远程 URL 列表
   * #103 修复：不再直接使用 tempFilePaths 提交
   */
  async uploadImages(filePaths: string[]): Promise<string[]> {
    const urls: string[] = [];
    for (const filePath of filePaths) {
      try {
        const res = await wx.uploadFile({
          url: `${getApp().globalData.apiBaseUrl || 'https://api.dabashou.example.com'}/api/v1/upload/image`,
          filePath,
          name: 'file',
          header: {
            Authorization: `Bearer ${getApp().globalData.token || ''}`,
          },
        });
        const data = JSON.parse(res.data);
        if (data.code === 200 && data.data) {
          urls.push(data.data as string);
        } else {
          console.error('[PublishDemand] 图片上传失败:', data);
        }
      } catch (err) {
        console.error('[PublishDemand] 图片上传异常:', err);
      }
    }
    return urls;
  },
});
