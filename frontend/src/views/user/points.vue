<template>
  <div class="points-page">
    <div class="page-container">
      <h2 class="page-title">积分管理</h2>

      <!-- 积分卡片 -->
      <div class="balance-cards">
        <div class="balance-card gradient-primary">
          <span class="balance-label">可用积分</span>
          <span class="balance-value">{{ balance.available }}</span>
        </div>
        <div class="balance-card" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
          <span class="balance-label">冻结中</span>
          <span class="balance-value">{{ balance.frozen }}</span>
        </div>
        <div class="balance-card" style="background: linear-gradient(135deg, #1e293b, #475569);">
          <span class="balance-label">累计获得</span>
          <span class="balance-value">{{ balance.total }}</span>
        </div>
      </div>

      <!-- 流水明细 -->
      <div class="transactions-card">
        <div class="card-header">
          <h3>积分流水</h3>
          <el-select v-model="typeFilter" size="small" placeholder="全部类型" clearable style="width: 140px" @change="search">
            <el-option label="收入" :value="1" />
            <el-option label="支出" :value="2" />
            <el-option label="冻结" :value="3" />
            <el-option label="解冻" :value="4" />
            <el-option label="系统奖励" :value="5" />
            <el-option label="系统扣除" :value="6" />
          </el-select>
        </div>

        <el-table :data="transactions" stripe v-loading="txLoading" empty-text="暂无积分流水">
          <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="txTypeTag(row.type)" size="small">{{ PointTransactionTypeMap[row.type as PointTransactionType] || '未知' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100" align="right">
            <template #default="{ row }">
              <span :class="row.type === 1 || row.type === 4 || row.type === 5 ? 'text-success' : 'text-danger'">
                {{ formatPoints(row.amount) }} 积分
              </span>
            </template>
          </el-table-column>
          <el-table-column label="余额" width="100" align="right">
            <template #default="{ row }">
              {{ row.balanceAfter }} 积分
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="txPage"
            :page-size="txSize"
            :total="txTotal"
            layout="prev, pager, next"
            background
            size="small"
            @current-change="changeTxPage"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getBalance, getTransactions } from '@/api/point'
import { formatDateTime, formatPoints } from '@/utils/format'
import { PointTransactionTypeMap } from '@/types/api'
import type { PointTransVo, PointTransactionType } from '@/types/api'

const balance = reactive({ available: 0, frozen: 0, total: 0 })
const transactions = ref<PointTransVo[]>([])
const txLoading = ref(false)
const txPage = ref(1)
const txSize = ref(10)
const txTotal = ref(0)
const typeFilter = ref<number | undefined>(undefined)

async function fetchBalance() {
  try {
    const result = await getBalance()
    balance.available = result.available
    balance.frozen = result.frozen
    balance.total = result.balance
  } catch {
    // handled
  }
}

async function fetchTransactions() {
  txLoading.value = true
  try {
    const result = await getTransactions({
      page: txPage.value,
      size: txSize.value,
      type: typeFilter.value,
    })
    transactions.value = result.list
    txTotal.value = result.total
  } catch {
    // handled
  } finally {
    txLoading.value = false
  }
}

function search() {
  txPage.value = 1
  fetchTransactions()
}

function changeTxPage(p: number) {
  txPage.value = p
  fetchTransactions()
}

function txTypeTag(type: number): 'success' | 'danger' | 'warning' | 'info' | undefined {
  if ([1, 4, 5].includes(type)) return 'success'
  if ([2, 3].includes(type)) return 'warning'
  if (type === 6) return 'danger'
  return 'info'
}

onMounted(() => {
  fetchBalance()
  fetchTransactions()
})
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.balance-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.balance-card {
  border-radius: $radius-lg;
  padding: $spacing-xl;
  color: #ffffff;
  text-align: center;

  .balance-label {
    display: block;
    font-size: $font-size-sm;
    opacity: 0.85;
  }

  .balance-value {
    display: block;
    font-size: 32px;
    font-weight: 700;
    margin-top: 8px;
  }
}

.transactions-card {
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  border: 1px solid $color-border;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-md;

    h3 {
      margin: 0;
      font-size: $font-size-lg;
      font-weight: 600;
    }
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}

.text-success { color: $color-success; font-weight: 600; }
.text-danger { color: $color-danger; font-weight: 600; }

@media (max-width: 768px) {
  .balance-cards {
    grid-template-columns: 1fr;
  }
}
</style>
