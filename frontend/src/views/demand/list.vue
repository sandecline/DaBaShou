<template>
  <div class="demand-list-page">
    <div class="page-container">
      <div class="list-toolbar">
        <div class="toolbar-left">
          <h2>求助看板</h2>
          <el-button type="primary" @click="$router.push('/demand/publish')">
            <el-icon><Plus /></el-icon>
            发布求助
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="keyword"
            placeholder="搜索需求..."
            prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="search"
            @clear="search"
          />
        </div>
      </div>

      <!-- 筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" size="small" @change="search">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button :value="1">待接单</el-radio-button>
          <el-radio-button :value="2">进行中</el-radio-button>
        </el-radio-group>

        <div class="filter-actions">
          <el-checkbox v-model="onlyUrgent" size="small" @change="search">仅看急单</el-checkbox>
          <el-select v-model="sortBy" size="small" style="width: 130px" @change="search">
            <el-option label="最新发布" value="latest" />
            <el-option label="悬赏最高" value="reward" />
            <el-option label="距离最近" value="distance" />
          </el-select>
        </div>
      </div>

      <LoadingSpinner v-if="loading" text="加载中..." />
      <template v-else>
        <div v-if="list.length > 0" class="card-grid">
          <DemandCard v-for="demand in list" :key="demand.id" :demand="demand" />
        </div>
        <EmptyState v-else icon="📝" title="暂无求助需求" description="你是第一个发出求助的人吗？">
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
import { searchDemands } from '@/api/demand'
import DemandCard from '@/components/common/DemandCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { DemandItemVo } from '@/types/api'

const loading = ref(true)
const list = ref<DemandItemVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const keyword = ref('')
const statusFilter = ref(0)
const onlyUrgent = ref(false)
const sortBy = ref('latest')

async function fetchData() {
  loading.value = true
  try {
    const result = await searchDemands({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      urgent: onlyUrgent.value ? 1 : undefined,
      sort: sortBy.value,
    })
    list.value = result.list.map((d) => ({
      ...d,
      isUrgent: d.deadline ? new Date(d.deadline).getTime() - Date.now() < 12 * 3600 * 1000 : false,
    }))
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

onMounted(fetchData)
</script>

<style scoped lang="scss">
.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: 700;
    }
  }

  .search-input {
    width: 240px;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
  flex-wrap: wrap;
  gap: 12px;

  .filter-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}
</style>
