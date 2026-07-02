/**
 * 发布技能表单页
 * 填写技能信息并发布到技能货架
 */

import { skillService } from '../../services/skill';
import { shelfService } from '../../services/shelf';
import { fileService } from '../../services/file';
import { ensureLogin } from '../../utils/auth';
import { isTitleValid, isDescValid, isPointPrice } from '../../utils/validator';
import type { SkillCategory, SkillTag, PublishSkillParams, LocationType } from '../../types/skill';
import type { SkillShelfForm, TimeSlot } from '../../types/shelf';

/** 位置类型选项 */
const LOCATION_OPTIONS = [
  { label: '线上', value: 1 },
  { label: '线下', value: 2 },
  { label: '均可', value: 3 },
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
    pointPrice: '',
    durationMinutes: '',
    locationType: 1 as LocationType,
    locationOptions: LOCATION_OPTIONS,

    // 空闲时段
    timeSlots: [] as TimeSlot[],
    addingTimeSlot: false,
    todayStr: '',
    newSlotDate: '',
    newSlotStart: '08:00',
    newSlotEnd: '10:00',

    // 图片
    images: [] as string[],
    maxImages: 9,

    // 提交状态
    submitting: false,

    // 编辑模式
    editMode: false,
    editId: 0,

    // 字符计数
    titleLength: 0,
    descLength: 0,
  },

  async onLoad(options: Record<string, string | undefined>) {
    // Always safe guard: store timers for cleanup on unload
    (this as any)._navigateTimer = null as number | null;

    // 初始化日期（今天 + 明天默认值）
    const today = new Date();
    const p = (n: number) => String(n).padStart(2, '0');
    const todayStr = `${today.getFullYear()}-${p(today.getMonth() + 1)}-${p(today.getDate())}`;
    const tomorrow = new Date(today.getTime() + 86400000);
    const tomorrowStr = `${tomorrow.getFullYear()}-${p(tomorrow.getMonth() + 1)}-${p(tomorrow.getDate())}`;
    this.setData({ todayStr, newSlotDate: tomorrowStr });

    // #92 修复：检查登录状态
    const loggedIn = await ensureLogin();
    if (!loggedIn) {
      wx.showToast({ title: '请先登录', icon: 'error' });
      this.navigateBackSafe(1000);
      return;
    }
    // 先加载分类
    await this.loadCategories();
    // 编辑模式：加载已有技能数据
    const editId = Number(options.editId);
    if (editId) {
      this.setData({ editMode: true, editId });
      wx.setNavigationBarTitle({ title: '编辑技能' });
      this.loadEditData(editId);
    }
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
      // Mock 降级
      this.setData({
        categories: [{ id: 0, name: '加载失败', icon: '', sortOrder: 0 }],
        tags: [],
      });
    }
  },

  /** 加载已有技能数据填充表单 */
  async loadEditData(shelfId: number) {
    try {
      const res = await shelfService.getDetail(shelfId);
      const skill = (res.data && typeof res.data === 'object') ? res.data : (res as unknown as { title?: string; description?: string; pointPrice?: number; durationMinutes?: number; locationType?: number; skillTagId?: number; images?: string[]; });
      if (!skill) return;

      // 计算 categoryIndex / tagIndex
      let catIdx = 0; let tagIdx = 0;
      const { categories } = this.data;
      if (categories.length > 0 && skill.skillTagId) {
        for (let i = 0; i < categories.length; i++) {
          const tag = categories[i].tags?.find((t) => t.id === skill.skillTagId);
          if (tag) { catIdx = i; tagIdx = categories[i].tags?.indexOf(tag) ?? 0; break; }
        }
      }

      this.setData({
        title: skill.title || '',
        description: skill.description || '',
        pointPrice: String(skill.pointPrice ?? ''),
        durationMinutes: String(skill.durationMinutes ?? ''),
        locationType: (skill.locationType || 1) as LocationType,
        images: (skill as any).images || [],
        timeSlots: (skill as any).timeSlots || [],
        categoryIndex: catIdx,
        tagIndex: tagIdx,
        tags: categories[catIdx]?.tags || [],
        titleLength: (skill.title || '').length,
        descLength: (skill.description || '').length,
      });
    } catch (err) {
      console.error('加载技能数据失败:', err);
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

  // ===== 位置选择 =====

  onLocationChange(e: WechatMiniprogram.CustomEvent) {
    this.setData({ locationType: Number(e.detail.value) as LocationType });
  },

  // ===== 空闲时段管理 =====

  onShowTimeSlotEditor() {
    this.setData({ addingTimeSlot: true });
  },

  onCancelTimeSlot() {
    this.setData({ addingTimeSlot: false });
  },

  onSlotDateChange(e: WechatMiniprogram.PickerChange) {
    this.setData({ newSlotDate: e.detail.value as string });
  },

  onSlotStartChange(e: WechatMiniprogram.PickerChange) {
    this.setData({ newSlotStart: e.detail.value as string });
  },

  onSlotEndChange(e: WechatMiniprogram.PickerChange) {
    this.setData({ newSlotEnd: e.detail.value as string });
  },

  onConfirmTimeSlot() {
    const { newSlotDate, newSlotStart, newSlotEnd } = this.data;
    if (!newSlotDate) {
      wx.showToast({ title: '请选择日期', icon: 'none' });
      return;
    }
    if (!newSlotStart || !newSlotEnd) {
      wx.showToast({ title: '请选择起止时间', icon: 'none' });
      return;
    }
    if (newSlotStart >= newSlotEnd) {
      wx.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' });
      return;
    }
    // 检查与已有时段是否重叠
    const overlaps = this.data.timeSlots.some(
      (s) => s.date === newSlotDate
        && !(s.endTime <= newSlotStart || s.startTime >= newSlotEnd)
    );
    if (overlaps) {
      wx.showToast({ title: '该时段与已有时段重叠', icon: 'none' });
      return;
    }

    const newSlot: TimeSlot = {
      date: newSlotDate,
      startTime: newSlotStart,
      endTime: newSlotEnd,
    };
    const timeSlots = [...this.data.timeSlots, newSlot].sort((a, b) => {
      const d = a.date.localeCompare(b.date);
      return d !== 0 ? d : a.startTime.localeCompare(b.startTime);
    });
    this.setData({ timeSlots, addingTimeSlot: false });
  },

  onRemoveTimeSlot(e: WechatMiniprogram.CustomEvent) {
    const { index } = e.currentTarget.dataset;
    const timeSlots = [...this.data.timeSlots];
    timeSlots.splice(index, 1);
    this.setData({ timeSlots });
  },

  // ===== 表单输入 =====
  // 输入节流：避免每次按键都触发 setData（对 textarea 尤其重要）

  onTitleInput(e: WechatMiniprogram.Input) {
    const val = e.detail.value;
    if ((this as any)._titleTimer) clearTimeout((this as any)._titleTimer);
    (this as any)._titleTimer = setTimeout(() => {
      this.setData({ title: val, titleLength: val.length });
    }, 100);
  },

  onDescInput(e: WechatMiniprogram.TextareaInput) {
    const val = e.detail.value;
    if ((this as any)._descTimer) clearTimeout((this as any)._descTimer);
    (this as any)._descTimer = setTimeout(() => {
      this.setData({ description: val, descLength: val.length });
    }, 150);
  },

  onPriceInput(e: WechatMiniprogram.Input) {
    this.setData({ pointPrice: e.detail.value });
  },

  onDurationInput(e: WechatMiniprogram.Input) {
    this.setData({ durationMinutes: e.detail.value });
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
    const { title, description, pointPrice, durationMinutes, tags, tagIndex } = this.data;

    if (!isTitleValid(title)) {
      return '标题需 2-50 个字';
    }
    if (!isDescValid(description)) {
      return '描述需 5-500 个字';
    }
    const price = Number(pointPrice);
    if (!isPointPrice(price)) {
      return '积分价格需在 1-9999 之间';
    }
    const duration = Number(durationMinutes);
    if (!Number.isInteger(duration) || duration < 1 || duration > 1440) {
      return '预计时长需在 1-1440 分钟之间';
    }
    if (!tags.length || !tags[tagIndex]) {
      return '请选择技能分类和标签';
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

    // #92 修复：再次确认登录态
    if (!getApp().globalData.token) {
      wx.showToast({ title: '登录已过期，请重新进入', icon: 'error' });
      return;
    }

    const { tagIndex, tags, title, description, pointPrice, durationMinutes, locationType, images, editMode, editId } = this.data;

    wx.showLoading({ title: editMode ? '保存中...' : '发布中...', mask: true });
    this.setData({ submitting: true });

    try {
      // #93 修复：先将本地图片上传到服务器获取远程 URL
      let remoteImages: string[] = [];
      if (images && images.length > 0) {
        remoteImages = await this.uploadImages(images);
      }

      const params: PublishSkillParams = {
        skillTagId: tags[tagIndex].id,
        title: title.trim(),
        description: description.trim(),
        pointPrice: Number(pointPrice),
        durationMinutes: Number(durationMinutes),
        locationType,
        images: remoteImages,
        timeSlots: this.data.timeSlots,
      };

      wx.hideLoading();

      if (editMode && editId) {
        await shelfService.update(editId, params as unknown as SkillShelfForm);
        wx.showToast({ title: '保存成功', icon: 'success', duration: 1200 });
        this.navigateBackSafe(1200);
      } else {
        const pubRes = await shelfService.publish(params as unknown as SkillShelfForm);
        wx.showToast({ title: '发布成功', icon: 'success', duration: 1200 });
        (this as any)._navigateTimer = setTimeout(() => {
          wx.redirectTo({ url: `/pages/skill-detail/skill-detail?id=${pubRes.data.id}` });
        }, 1200) as unknown as number;
      }
    } catch (err) {
      wx.hideLoading();
      console.error('发布技能失败:', err);
      wx.showToast({ title: '发布失败，请重试', icon: 'error' });
    } finally {
      this.setData({ submitting: false });
    }
  },

  /** 并行上传图片，返回远程 URL 列表 */
  async uploadImages(filePaths: string[]): Promise<string[]> {
    return fileService.uploadBatch(filePaths);
  },

  onUnload() {
    // 清理所有定时器，防止页面销毁后触发导航
    const self = this as any;
    if (self._titleTimer) clearTimeout(self._titleTimer);
    if (self._descTimer) clearTimeout(self._descTimer);
    if (self._navigateTimer) clearTimeout(self._navigateTimer);
  },

  /** 安全延迟返回上一页，防止页面销毁后 navigateBack 报错 */
  navigateBackSafe(delayMs: number) {
    (this as any)._navigateTimer = setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) wx.navigateBack();
      else wx.switchTab({ url: '/pages/index/index' });
    }, delayMs) as unknown as number;
  },
});
