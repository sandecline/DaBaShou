/**
 * 首页 — 技能浏览 + 需求看板
 */

import { skillService } from '../../services/skill';
import { shelfService } from '../../services/shelf';
import { demandService } from '../../services/demand';
import type { SkillCategory } from '../../types/skill';
import type { SkillShelf } from '../../types/shelf';
import type { Demand } from '../../types/demand';

Page({
  data: {
    /** 当前Tab：0-技能 1-需求 */
    activeTab: 0,
    /** 技能分类列表 */
    categories: [] as SkillCategory[],
    /** 当前选中的分类ID */
    activeCategoryId: 0,
    /** 技能列表 */
    skillList: [] as SkillShelf[],
    /** 需求列表 */
    demandList: [] as Demand[],
    /** 搜索关键词 */
    searchKeyword: '',
    /** 加载状态 */
    loading: true,
    /** 分页 */
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
  },

  onLoad() {
    this.loadCategories();
    this.loadSkillList();
  },

  onShow() {
    // 每次显示时刷新未读消息数
    this.updateUnreadBadge();
  },

  // 下拉刷新
  async onPullDownRefresh() {
    this.setData({ pageNum: 1, hasMore: true });
    if (this.data.activeTab === 0) {
      await this.loadSkillList();
    } else {
      await this.loadDemandList();
    }
    wx.stopPullDownRefresh();
  },

  // 上拉加载更多
  onReachBottom() {
    if (!this.data.hasMore) return;
    const nextPage = this.data.pageNum + 1;
    this.data.pageNum = nextPage; // 直接改 data，不走 setData 渲染
    if (this.data.activeTab === 0) {
      this.loadSkillList(true);
    } else {
      this.loadDemandList(true);
    }
  },

  // Tab 切换
  onTabChange(e: WechatMiniprogram.CustomEvent) {
    const index = e.detail.value;
    this.setData({ activeTab: index, pageNum: 1, hasMore: true });
    if (index === 0) {
      this.loadSkillList();
    } else {
      this.loadDemandList();
    }
  },

  // 分类切换
  onCategoryChange(e: WechatMiniprogram.CustomEvent) {
    const id = e.currentTarget.dataset.id;
    this.setData({ activeCategoryId: id, pageNum: 1, hasMore: true });
    this.loadSkillList();
  },

  // 搜索
  onSearch(e: WechatMiniprogram.CustomEvent) {
    this.setData({ searchKeyword: e.detail.value, pageNum: 1, hasMore: true });
    if (this.data.activeTab === 0) {
      this.loadSkillList();
    } else {
      this.loadDemandList();
    }
  },

  // 跳转技能详情
  goSkillDetail(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/skill-detail/skill-detail?id=${id}` });
  },

  // 跳转需求详情
  goDemandDetail(e: WechatMiniprogram.CustomEvent) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/demand-detail/demand-detail?id=${id}` });
  },

  // ===== 数据加载 =====

  async loadCategories() {
    try {
      const res = await skillService.getCategories();
      this.setData({ categories: res.data });
    } catch (err) {
      console.error('加载分类失败:', err);
    }
  },

  async loadSkillList(append = false) {
    try {
      const { activeCategoryId, searchKeyword, pageNum, pageSize } = this.data;
      const res = await shelfService.search({
        categoryId: activeCategoryId || undefined,
        keyword: searchKeyword || undefined,
        pageNum,
        pageSize,
      });
      const newList = append ? [...this.data.skillList, ...res.data.list] : res.data.list;
      this.setData({
        skillList: newList,
        hasMore: newList.length < res.data.total,
        loading: false,
      });
    } catch (err) {
      console.error('加载技能列表失败:', err);
      this.setData({ loading: false });
    }
  },

  async loadDemandList(append = false) {
    try {
      const { searchKeyword, pageNum, pageSize } = this.data;
      const res = await demandService.search({
        keyword: searchKeyword || undefined,
        pageNum,
        pageSize,
      });
      const newList = append ? [...this.data.demandList, ...res.data.list] : res.data.list;
      this.setData({
        demandList: newList,
        hasMore: newList.length < res.data.total,
        loading: false,
      });
    } catch (err) {
      console.error('加载需求列表失败:', err);
      this.setData({ loading: false });
    }
  },

  updateUnreadBadge() {
    const unread = getApp().globalData.unreadCount;
    if (unread > 0) {
      wx.setTabBarBadge({ index: 2, text: String(unread > 99 ? '99+' : unread) });
    } else {
      wx.removeTabBarBadge({ index: 2 });
    }
  },

  onShareAppMessage() {
    return {
      title: '搭把手 - 校园技能共享平台',
      path: '/pages/index/index',
    };
  },

  onShareTimeline() {
    return {
      title: '搭把手 - 校园技能共享',
    };
  },
});
