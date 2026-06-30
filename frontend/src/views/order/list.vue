<template>
  <div class="order-list-page">
    <div class="page-container">
      <h2 class="page-title">我的订单</h2>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我发布的" name="my" />
        <el-tab-pane label="我接的" name="taken" />
      </el-tabs>

      <!-- 状态筛选 -->
      <div class="status-filter">
        <el-radio-group v-model="statusFilter" size="small" @change="search">
          <el-radio-button :value="-1">全部</el-radio-button>
          <el-radio-button :value="1">待支付</el-radio-button>
          <el-radio-button :value="2">已支付</el-radio-button>
          <el-radio-button :value="3">服务中</el-radio-button>
          <el-radio-button :value="4">待确认</el-radio-button>
          <el-radio-button :value="5">已完成</el-radio-button>
          <el-radio-button :value="7">争议中</el-radio-button>
        </el-radio-group>
      </div>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <div v-if="list.length > 0" class="order-list">
          <OrderCard v-for="order in list" :key="order.id" :order="order" />
        </div>
        <EmptyState v-else icon="📋" title="暂无订单" />
      </template>

      <div v-if="total > size" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="changePage"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrderList } from '@/api/order'
import OrderCard from '@/components/common/OrderCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { OrderItemVo } from '@/types/api'

const loading = ref(true)
const list = ref<OrderItemVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeTab = ref('my')
const statusFilter = ref(-1)

async function fetchData() {
  loading.value = true
  try {
    const fetchFn = activeTab.value === 'my' ? getMyOrders : getMyTakenOrders
    const result = await fetchFn({
      page: page.value,
      size: size.value,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined,
    })
    list.value = result.list
    total.value = result.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchData()
}

function changePage(p: number) {
  page.value = p
  fetchData()
}

function handleTabChange() {
  page.value = 1
  statusFilter.value = -1
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.status-filter {
  margin-bottom: $spacing-lg;
  overflow-x: auto;
  white-space: nowrap;
  padding-bottom: 4px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}
</style>
