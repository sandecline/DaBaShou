/**
 * 信用评价页
 * 展示信任分汇总 + 评价列表
 */

import { creditService } from '../../../services/credit';
import type { Review } from '../../../types/credit';
import type { PageResult } from '../../../types/api-response';

/** 评价 Tab */
type ReviewTab = 'received' | 'given';

interface ReviewTabItem {
  label: string;
  value: ReviewTab;
}

Page({
  data: {
    /** 用户信任分 */
    trustScore: 0,
    /** 信任等级 */
    trustLevel: '' as string,
    /** 评价列表 */
    reviewList: [] as Review[],
    /** Tab 列表 */
    reviewTabs: [
      { label: '收到的评价', value: 'received' },
      { label: '发出的评价', value: 'given' },
    ] as ReviewTabItem[],
    /** 当前选中 Tab */
    activeTab: 'received' as ReviewTab,
    /** 加载状态 */
    loading: true,
    /** 数据为空 */
    empty: false,
    /** 分页 */
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loadingMore: false,
  },

  onLoad() {
    this.loadData();
  },

  async loadData(reset = false) {
    const { activeTab, pageNum, pageSize, loadingMore } = this.data;
    if (loadingMore) return;

    const newPageNum = reset ? 1 : pageNum;

    try {
      this.setData({
        loading: reset ? true : this.data.loading,
        loadingMore: !reset && pageNum > 1,
      });

      const app = getApp();
      const userInfo = app.globalData.userInfo;
      const userId = userInfo?.id;

      if (!userId) {
        this.setData({ loading: false });
        return;
      }

      // 设置信任分
      if (userInfo.trustScore !== undefined) {
        this.setData({
          trustScore: userInfo.trustScore,
          trustLevel: userInfo.trustLevel || '',
        });
      }

      // 暂只加载收到的评价（发出的评价接口与收到的相同，由后端根据 token 区分）
      const res = await creditService.getUserReviews(userId, {
        pageNum: newPageNum,
        pageSize,
      });

      const result = (res.data || res) as PageResult<Review>;
      const list = result.records || [];

      this.setData({
        reviewList: reset ? list : [...this.data.reviewList, ...list],
        pageNum: newPageNum,
        hasMore: list.length >= pageSize,
        empty: reset && list.length === 0,
        loading: false,
        loadingMore: false,
      });
    } catch (err) {
      console.error('[Credit] 加载评价失败:', err);
      this.setData({
        loading: false,
        loadingMore: false,
        empty: reset,
      });
    }
  },

  /** 切换 Tab */
  onTabChange(e: WechatMiniprogram.CustomEvent) {
    const { value } = e.detail as { value: ReviewTab };
    this.setData({ activeTab: value });
    this.loadData(true);
  },

  /** 上拉加载更多 */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadData();
    }
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.loadData(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /** 获取信任分等级标签色 */
  getTrustLevelColor(): string {
    const colorMap: Record<string, string> = {
      '金牌': '#ff8c00',
      '靠谱': '#2ba471',
      '新人': '#0052d9',
    };
    return colorMap[this.data.trustLevel] || '#999';
  },
});
