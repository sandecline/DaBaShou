<template>
  <div class="my-demands-page">
    <div class="page-container">
      <div class="list-toolbar">
        <div class="toolbar-left">
          <h2>我的求助</h2>
          <el-button type="primary" @click="$router.push('/demand/publish')">
            <el-icon><Plus /></el-icon>
            发布求助
          </el-button>
        </div>
      </div>

      <!-- 状态筛选 -->
      <div class="status-filter">
        <el-radio-group v-model="statusFilter" size="small" @change="search">
          <el-radio-button :value="-1">全部</el-radio-button>
          <el-radio-button :value="1">待接单</el-radio-button>
          <el-radio-button :value="2">进行中</el-radio-button>
          <el-radio-button :value="3">已完成</el-radio-button>
          <el-radio-button :value="0">已关闭</el-radio-button>
        </el-radio-group>
      </div>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <div v-if="list.length > 0" class="demand-list">
          <div v-for="demand in list" :key="demand.id" class="demand-card">
            <div class="card-header">
              <span class="demand-title" @click="$router.push(`/demand/${demand.id}`)">
                {{ demand.title }}
              </span>
              <el-tag :type="getStatusType(demand.status)" size="small">
                {{ getStatusDesc(demand.status) }}
              </el-tag>
            </div>

            <div class="card-body">
              <div class="demand-info">
                <span class="reward">悬赏: {{ demand.pointReward }} 积分</span>
                <span class="time">{{ formatTime(demand.createTime) }}</span>
              </div>
              <div v-if="demand.skillTagName" class="tag">
                <el-tag size="small" type="info">{{ demand.skillTagName }}</el-tag>
              </div>
            </div>

            <div class="card-actions">
              <el-button
                v-if="demand.status === 1"
                size="small"
                @click="$router.push(`/demand/${demand.id}`)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="demand.status === 1"
                size="small"
                type="warning"
                @click="handleClose(demand.id)"
              >
                关闭求助
              </el-button>
              <el-button
                v-if="demand.status === 0 || demand.status === 3"
                size="small"
                type="danger"
                @click="handleDelete(demand.id)"
              >
                删除
              </el-button>
              <el-button
                v-if="demand.status === 2"
                size="small"
                type="success"
                @click="$router.push(`/demand/${demand.id}`)"
              >
                查看进度
              </el-button>
            </div>
          </div>
        </div>
        <EmptyState v-else icon="📝" title="暂无求助" description="你还没有发布过求助需求">
          <el-button type="primary" @click="$router.push('/demand/publish')">发布求助</el-button>
        </EmptyState>
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
import { ElMessageBox, ElMessage } from 'element-plus'
import { getMyDemands, closeDemand, deleteDemand } from '@/api/demand'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { DemandItemVo } from '@/types/api'

const loading = ref(true)
const list = ref<DemandItemVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref(-1)

async function fetchData() {
  loading.value = true
  try {
    const result = await getMyDemands({
      pageNum: page.value,
      pageSize: size.value,
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

async function handleClose(id: number) {
  try {
    await ElMessageBox.confirm('确定要关闭这个求助吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await closeDemand(id)
    ElMessage.success('求助已关闭')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个求助吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDemand(id)
    ElMessage.success('求助已删除')
    fetchData()
  } catch {
    // cancelled
  }
}

function getStatusType(status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'primary',
  }
  return map[status] || 'info'
}

function getStatusDesc(status: number) {
  const map: Record<number, string> = {
    0: '已关闭',
    1: '待接单',
    2: '进行中',
    3: '已完成',
  }
  return map[status] || '未知'
}

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return time.substring(0, 10)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.my-demands-page {
  padding: 20px 0;
}

.page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 16px;

    h2 {
      margin: 0;
      font-size: 24px;
    }
  }
}

.status-filter {
  margin-bottom: 20px;
}

.demand-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.demand-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .demand-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      cursor: pointer;
      flex: 1;
      margin-right: 12px;

      &:hover {
        color: #409eff;
      }
    }
  }

  .card-body {
    margin-bottom: 12px;

    .demand-info {
      display: flex;
      gap: 16px;
      color: #666;
      font-size: 14px;

      .reward {
        color: #e6a23c;
        font-weight: 500;
      }
    }

    .tag {
      margin-top: 8px;
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
