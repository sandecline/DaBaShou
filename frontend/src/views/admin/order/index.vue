<template>
  <div class="admin-page">
    <div class="admin-layout">
      <Sidebar :menu-items="adminMenu" />
      <div class="admin-content">
        <div class="page-container">
          <h2>订单管理</h2>

          <div class="toolbar">
            <el-input
              v-model="keyword"
              placeholder="搜索订单号..."
              prefix-icon="Search"
              clearable
              style="width: 240px"
              @keyup.enter="search"
              @clear="search"
            />
            <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 140px" @change="search">
              <el-option v-for="(label, val) in OrderStatusMap" :key="val" :label="label" :value="Number(val)" />
            </el-select>
          </div>

          <el-table :data="list" stripe v-loading="loading" border>
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="title" label="服务标题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="buyerName" label="买家" width="100" />
            <el-table-column prop="sellerName" label="卖家" width="100" />
            <el-table-column label="金额" width="90">
              <template #default="{ row }">{{ row.pointAmount }} 积分</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :color="getOrderStatusColor(row.status)" size="small" effect="dark" style="border:none;color:#fff">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="showDetail(row)">详情</el-button>
                <el-button
                  v-if="row.status === 7"
                  size="small"
                  type="warning"
                  @click="handleDispute(row)"
                >
                  处理争议
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

          <!-- 争议处理对话框 -->
          <el-dialog v-model="disputeDialog" title="处理争议订单" width="400px">
            <p>订单号：{{ disputeOrder?.orderNo }}</p>
            <el-radio-group v-model="disputeAction">
              <el-radio value="complete">判定完成（积分给卖家）</el-radio>
              <el-radio value="refund">判定退款（积分退买家）</el-radio>
            </el-radio-group>
            <template #footer>
              <el-button @click="disputeDialog = false">取消</el-button>
              <el-button type="primary" :loading="disputing" @click="confirmDispute">确认处理</el-button>
            </template>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrderList, handleDisputeOrder } from '@/api/admin'
import { getOrderStatusText, getOrderStatusColor, formatDateTime } from '@/utils/format'
import { OrderStatusMap } from '@/types'
import Sidebar from '@/components/layout/Sidebar.vue'
import type { Order } from '@/types'

const adminMenu = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Document' },
  { path: '/admin/credit', title: '信用管理', icon: 'Warning' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const loading = ref(false)
const list = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

// Dispute
const disputeDialog = ref(false)
const disputeOrder = ref<Order | null>(null)
const disputeAction = ref<'complete' | 'refund'>('complete')
const disputing = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const result = await getAdminOrderList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value,
    })
    list.value = result.records
    total.value = result.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function search() { page.value = 1; fetchData() }
function changePage(p: number) { page.value = p; fetchData() }

function showDetail(order: Order) {
  ElMessageBox.alert(
    `订单号：${order.orderNo}\n标题：${order.title}\n金额：${order.pointAmount}积分\n状态：${getOrderStatusText(order.status)}`,
    '订单详情',
  )
}

function handleDispute(order: Order) {
  disputeOrder.value = order
  disputeAction.value = 'complete'
  disputeDialog.value = true
}

async function confirmDispute() {
  if (!disputeOrder.value) return
  disputing.value = true
  try {
    await handleDisputeOrder(disputeOrder.value.id, disputeAction.value)
    ElMessage.success('争议已处理')
    disputeDialog.value = false
    fetchData()
  } catch {
    // handled
  } finally {
    disputing.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: calc(100vh - #{$header-offset});
}

.admin-content {
  flex: 1;
  overflow-x: auto;
}

h2 {
  margin: 0 0 $spacing-md;
  font-size: $font-size-xl;
  font-weight: 700;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: $spacing-md;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}
</style>
