<template>
  <AdminLayout
    title="订单仲裁"
    eyebrow="ORDER CONTROL"
    subtitle="查看订单状态和交易内容，集中处理争议订单。"
  >
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索订单号、标题、用户"
        prefix-icon="Search"
        clearable
        style="width: 280px"
        @keyup.enter="search"
        @clear="search"
      >
        <template #append>
          <el-button @click="search">搜索</el-button>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 150px" @change="search">
        <el-option v-for="(label, val) in OrderStatusMap" :key="val" :label="label" :value="Number(val)" />
      </el-select>
    </template>

    <el-table :data="list" stripe v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column label="交易内容" min-width="210" show-overflow-tooltip>
        <template #default="{ row }">{{ orderTitle(row as OrderAdminVo) }}</template>
      </el-table-column>
      <el-table-column prop="buyerNickname" label="买家" width="120" />
      <el-table-column prop="sellerNickname" label="卖家" width="120" />
      <el-table-column label="积分" width="90">
        <template #default="{ row }">{{ row.pointAmount }} 积分</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :color="getOrderStatusColor(row.status)" size="small" effect="dark" style="border:none;color:#fff">
            {{ row.statusDesc || getOrderStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row as OrderAdminVo)">详情</el-button>
          <el-button
            v-if="row.status === 7"
            size="small"
            type="warning"
            @click="openArbitrate(row as OrderAdminVo)"
          >
            仲裁
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="changePage"
      />
    </div>

    <el-drawer v-model="detailVisible" title="订单详情" size="480px">
      <div v-if="detail" class="detail-list">
        <p><span>订单号</span>{{ detail.orderNo }}</p>
        <p><span>交易内容</span>{{ orderTitle(detail) }}</p>
        <p><span>买家</span>{{ detail.buyerNickname }}</p>
        <p><span>卖家</span>{{ detail.sellerNickname }}</p>
        <p><span>积分</span>{{ detail.pointAmount }}</p>
        <p><span>状态</span>{{ detail.statusDesc || getOrderStatusText(detail.status) }}</p>
        <p><span>备注</span>{{ detail.remark || '-' }}</p>
        <p><span>取消原因</span>{{ detail.cancelReason || '-' }}</p>
        <p><span>创建时间</span>{{ formatDateTime(detail.createTime) }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="arbitrateDialog" title="争议订单仲裁" width="460px">
      <el-form label-position="top">
        <el-form-item label="订单">
          <el-input :model-value="arbitrateOrder ? `${arbitrateOrder.orderNo} / ${orderTitle(arbitrateOrder)}` : ''" disabled />
        </el-form-item>
        <el-form-item label="仲裁结果">
          <el-radio-group v-model="arbitrateForm.result">
            <el-radio value="complete">判定完成，积分给卖家</el-radio>
            <el-radio value="refund">判定退款，积分退买家</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="仲裁理由">
          <el-input v-model="arbitrateForm.reason" type="textarea" :rows="3" placeholder="说明证据、沟通结果和处理依据" />
        </el-form-item>
        <el-form-item v-if="arbitrateForm.result === 'refund'" label="退款积分（可选）">
          <el-input-number v-model="arbitrateForm.refundAmount" :min="0" :max="arbitrateOrder?.pointAmount || 0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arbitrateDialog = false">取消</el-button>
        <el-button type="primary" :loading="arbitrating" @click="submitArbitrate">保存仲裁</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminArbitrateOrder, getAdminOrderDetail, getAdminOrderList } from '@/api/admin'
import { getOrderStatusText, getOrderStatusColor, formatDateTime } from '@/utils/format'
import { OrderStatusMap } from '@/types/api'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import type { OrderAdminVo } from '@/types/api'

const loading = ref(false)
const arbitrating = ref(false)
const list = ref<OrderAdminVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const detailVisible = ref(false)
const detail = ref<OrderAdminVo | null>(null)
const arbitrateDialog = ref(false)
const arbitrateOrder = ref<OrderAdminVo | null>(null)
const arbitrateForm = reactive({
  result: 'complete' as 'complete' | 'refund',
  reason: '',
  refundAmount: undefined as number | undefined,
})

async function fetchData() {
  loading.value = true
  try {
    const result = await getAdminOrderList({
      pageNum: page.value,
      pageSize: size.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value,
    })
    list.value = result.list
    total.value = result.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function orderTitle(order: OrderAdminVo) {
  return order.title || order.shelfTitle || order.demandTitle || '未命名交易'
}

function search() {
  page.value = 1
  fetchData()
}

function changePage(p: number) {
  page.value = p
  fetchData()
}

async function showDetail(order: OrderAdminVo) {
  try {
    detail.value = await getAdminOrderDetail(order.id)
    detailVisible.value = true
  } catch {
    // handled
  }
}

function openArbitrate(order: OrderAdminVo) {
  arbitrateOrder.value = order
  arbitrateForm.result = 'complete'
  arbitrateForm.reason = ''
  arbitrateForm.refundAmount = undefined
  arbitrateDialog.value = true
}

async function submitArbitrate() {
  if (!arbitrateOrder.value) return
  if (!arbitrateForm.reason.trim()) {
    ElMessage.warning('请填写仲裁理由')
    return
  }
  arbitrating.value = true
  try {
    await adminArbitrateOrder(arbitrateOrder.value.id, {
      result: arbitrateForm.result,
      reason: arbitrateForm.reason.trim(),
      refundAmount: arbitrateForm.result === 'refund' ? arbitrateForm.refundAmount : undefined,
    })
    ElMessage.success('仲裁结果已保存')
    arbitrateDialog.value = false
    fetchData()
  } catch {
    // handled
  } finally {
    arbitrating.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.detail-list {
  display: grid;
  gap: 12px;

  p {
    display: grid;
    grid-template-columns: 86px minmax(0, 1fr);
    gap: 10px;
    margin: 0;
    padding-bottom: 12px;
    border-bottom: 1px solid #edf2ef;
  }

  span {
    color: #667a74;
  }
}
</style>
