/**
 * 申诉中心
 * 选择违规记录 → 输入申诉理由 → 提交
 * 展示申诉记录列表
 */

import { creditService } from '../../../../services/credit';
import type { ViolationVo, AppealVo } from '../../../../types/credit';
import { formatDate } from '../../../../utils/date';

const ViolationTypeMap: Record<string, string> = {
  fake_service: '虚假服务',
  bad_attitude: '态度恶劣',
  illegal_trade: '违规交易',
  other: '其他',
};

Page({
  data: {
    violations: [] as ViolationVo[],
    violationLabels: [] as string[],
    violationPickerIndex: -1,
    selectedViolationId: null as number | null,
    reason: '',
    evidence: '',
    submitting: false,
    appeals: [] as AppealVo[],
    appealLoading: true,
  },

  onLoad() {
    this.loadViolations();
    this.loadAppeals();
  },

  async loadViolations() {
    try {
      const res = await creditService.getMyViolations({ pageNum: 1, pageSize: 50 });
      const list = (res.data?.list || []) as ViolationVo[];
      const eligible = list.filter((v) => v.status === 0);
      const labels = eligible.map(
        (v) => `${ViolationTypeMap[v.type] || v.type} - ${v.description}`
      );
      this.setData({
        violations: eligible,
        violationLabels: labels,
        violationPickerIndex: -1,
        selectedViolationId: null,
      });
    } catch (err) {
      console.error('[Appeal] 加载违规记录失败:', err);
    }
  },

  onViolationPickerChange(e: WechatMiniprogram.PickerChange) {
    const idx = Number(e.detail.value) || 0;
    const v = this.data.violations[idx];
    this.setData({
      violationPickerIndex: idx,
      selectedViolationId: v ? v.id : null,
    });
  },

  onReasonInput(e: WechatMiniprogram.Input) {
    this.setData({ reason: e.detail.value });
  },

  onEvidenceInput(e: WechatMiniprogram.Input) {
    this.setData({ evidence: e.detail.value });
  },

  async onSubmitAppeal() {
    const { selectedViolationId, reason, evidence, submitting } = this.data;
    if (submitting) return;
    if (!selectedViolationId) {
      wx.showToast({ title: '请选择违规记录', icon: 'error' });
      return;
    }
    if (!reason || reason.trim().length < 10) {
      wx.showToast({ title: '申诉理由至少10个字符', icon: 'error' });
      return;
    }
    this.setData({ submitting: true });
    try {
      await creditService.submitAppeal({
        violationId: selectedViolationId,
        reason: reason.trim(),
        evidence: evidence ? [evidence.trim()] : undefined,
      });
      wx.showToast({ title: '申诉已提交，等待审核', icon: 'success' });
      this.setData({ selectedViolationId: null, violationPickerIndex: -1, reason: '', evidence: '' });
      this.loadAppeals();
    } catch (err: unknown) {
      wx.showToast({ title: (err as Record<string, string>)?.msg || '提交失败', icon: 'error' });
    } finally {
      this.setData({ submitting: false });
    }
  },

  async loadAppeals() {
    this.setData({ appealLoading: true });
    try {
      const res = await creditService.getMyAppeals({ pageNum: 1, pageSize: 50 });
      const list = (res.data?.list || []) as AppealVo[];
      this.setData({
        appeals: list.map((a) => ({
          ...a,
          violationType: ViolationTypeMap[a.violationType] || a.violationType,
          _statusText: a.status === 0 ? '待审核' : a.status === 1 ? '已通过' : a.status === 2 ? '已驳回' : '未知',
          _statusTheme: a.status === 0 ? 'warning' : a.status === 1 ? 'success' : a.status === 2 ? 'danger' : 'default',
        })),
      });
    } catch (err) {
      console.error('[Appeal] 加载申诉记录失败:', err);
    } finally {
      this.setData({ appealLoading: false });
    }
  },

});
