<template>
  <div class="admin-page">
    <div class="admin-layout">
      <Sidebar :menu-items="adminMenu" />
      <div class="admin-content">
        <div class="page-container">
          <h2>用户管理</h2>

          <div class="toolbar">
            <el-input
              v-model="keyword"
              placeholder="搜索用户名、昵称、手机号..."
              prefix-icon="Search"
              clearable
              style="width: 300px"
              @keyup.enter="search"
              @clear="search"
            />
            <el-select v-model="statusFilter" placeholder="用户状态" clearable style="width: 140px" @change="search">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </div>

          <el-table :data="list" stripe v-loading="loading" border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" width="120" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column label="积分" width="100">
              <template #default="{ row }">{{ row.pointBalance }}</template>
            </el-table-column>
            <el-table-column label="信任分" width="90">
              <template #default="{ row }">{{ row.trustScore?.toFixed(1) }}</template>
            </el-table-column>
            <el-table-column label="校区" width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.campus || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="showDetail(row as UserAdminVo)">详情</el-button>
                <el-button
                  size="small"
                  :type="row.status === 1 ? 'danger' : 'success'"
                  @click="toggleStatus(row as UserAdminVo)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserList, updateUserStatus } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import Sidebar from '@/components/layout/Sidebar.vue'
import type { UserAdminVo } from '@/types/api'

const adminMenu = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Document' },
  { path: '/admin/credit', title: '信用管理', icon: 'Warning' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const loading = ref(false)
const list = ref<UserAdminVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

async function fetchData() {
  loading.value = true
  try {
    const result = await getAdminUserList({
      page: page.value,
      size: size.value,
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

function search() { page.value = 1; fetchData() }
function changePage(p: number) { page.value = p; fetchData() }

function showDetail(user: UserAdminVo) {
  ElMessageBox.alert(
    `用户名：${user.username}\n昵称：${user.nickname}\n积分：${user.pointBalance}\n信任分：${user.trustScore}\n校区：${user.campus || '-'}`,
    '用户详情',
  )
}

async function toggleStatus(user: UserAdminVo) {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await updateUserStatus(user.id, newStatus as 0 | 1)
    ElMessage.success('操作成功')
    fetchData()
  } catch {
    // handled
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
