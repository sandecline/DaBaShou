/**
 * 积分明细页
 * 展示积分账户余额 + 交易流水列表
 */

import { pointService } from '../../../services/point';
import type { PointAccount, PointTransaction } from '../../../types/point';
import type { PageResult } from '../../../types/api-response';

/** 筛选 Tab */
type FilterTab = 'all' | 'income' | 'expense';

interface FilterTabItem {
  label: string;
  value: FilterTab;
}

/** 收入类型集合 */
const INCOME_TYPES = [1, 4, 5, 7];

/** 判断是否为收入类型 */
function isIncomeType(type: number): boolean {
  return INCOME_TYPES.includes(type);
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

      const params: { pageNum: number; pageSize: number; type?: string } = {
        pageNum: newPageNum,
        pageSize,
      };

      if (activeTab === 'income') {
        params.type = '1,5,7';
      } else if (activeTab === 'expense') {
        params.type = '2,6';
      }

      const res = await pointService.getTransactions(params);
      const result = (res.data || res) as PageResult<PointTransaction>;
      const rawList = result.list || [];
      const list = rawList.map((t) => {
        const isIncome = isIncomeType(t.type);
        return {
          ...t,
          _isIncome: isIncome,
          _iconClass: isIncome ? 'icon-income' : 'icon-expense',
          _amountClass: isIncome ? 'amount-income' : 'amount-expense',
          _prefix: isIncome ? '+' : '-',
        };
      });

      this.setData({
        transactions: reset ? list : [...this.data.transactions, ...list],
        pageNum: newPageNum + 1,
        hasMore: list.length === pageSize,
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
});
