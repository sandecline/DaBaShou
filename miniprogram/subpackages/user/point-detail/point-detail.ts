/**
 * 积分明细页
 * 展示积分账户余额 + 交易流水列表
 */

import { pointService } from '../../../services/point';
import type { PointAccount, PointTransaction } from '../../../types/point';
import { POINT_TRANSACTION_TYPE_MAP } from '../../../utils/point-transaction';
import type { PageResult } from '../../../types/api-response';

/** 筛选 Tab */
type FilterTab = 'all' | 'income' | 'expense';

interface FilterTabItem {
  label: string;
  value: FilterTab;
}

Page({
  data: {
    /** 积分账户信息 */
    account: null as PointAccount | null,
    /** 交易流水列表 */
    transactions: [] as PointTransaction[],
    /** 筛选 Tab 列表 */
    filterTabs: [
      { label: '全部', value: 'all' },
      { label: '收入', value: 'income' },
      { label: '支出', value: 'expense' },
    ] as FilterTabItem[],
    /** 当前选中的筛选 Tab */
    activeTab: 'all' as FilterTab,
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
    this.loadAccount();
    this.loadTransactions();
  },

  /** 加载积分账户信息 */
  async loadAccount() {
    try {
      const res = await pointService.getBalance();
      const data = res.data || res;
      this.setData({ account: data as PointAccount });
    } catch (err) {
      console.error('[PointDetail] 获取积分账户失败:', err);
    }
  },

  /** 加载交易流水 */
  async loadTransactions(reset = false) {
    const { activeTab, pageNum, pageSize, loadingMore } = this.data;
    if (loadingMore) return;

    const newPageNum = reset ? 1 : pageNum;

    try {
      this.setData({
        loading: reset ? true : this.data.loading,
        loadingMore: !reset && pageNum > 1,
      });

      const params: { pageNum: number; pageSize: number; type?: number } = {
        pageNum: newPageNum,
        pageSize,
      };

      // 根据筛选 Tab 设置 type 参数
      // 1-收入, 2-支出, 3-冻结, 4-解冻, 5-系统奖励, 6-系统扣除
      if (activeTab === 'income') {
        // 收入和系统奖励归为收入类
        // 匹配 1(收入)+5(系统奖励)
        params.type = 1;
      } else if (activeTab === 'expense') {
        // 支出和系统扣除归为支出类
        params.type = 2;
      }

      const res = await pointService.getTransactions(params);
      const result = (res.data || res) as PageResult<PointTransaction>;
      const list = result.list || [];

      this.setData({
        transactions: reset ? list : [...this.data.transactions, ...list],
        pageNum: newPageNum,
        hasMore: list.length >= pageSize,
        empty: reset && list.length === 0,
        loading: false,
        loadingMore: false,
      });
    } catch (err) {
      console.error('[PointDetail] 获取交易流水失败:', err);
      this.setData({
        loading: false,
        loadingMore: false,
        empty: reset,
      });
    }
  },

  /** 切换筛选 Tab */
  onTabChange(e: WechatMiniprogram.CustomEvent) {
    const { value } = e.detail as { value: FilterTab };
    this.setData({ activeTab: value });
    this.loadTransactions(true);
  },

  /** 上拉加载更多 */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadTransactions();
    }
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.loadAccount();
    this.loadTransactions(true).finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /** 获取交易类型描述 */
  getTypeLabel(type: number): string {
    return POINT_TRANSACTION_TYPE_MAP[type as keyof typeof POINT_TRANSACTION_TYPE_MAP] || '未知';
  },

  /** 判断是否为收入类型 */
  isIncomeType(type: number): boolean {
    return [1, 4, 5].includes(type);
  },
});
