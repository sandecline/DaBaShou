<template>
  <AdminLayout
    title="用户管理"
    subtitle="查看用户资料、积分和信用状态，处理账号启用、禁用与密码重置"
  >
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名、昵称、手机号"
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
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="search">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </template>

    <el-table :data="list" stripe v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="34" :src="row.avatar">{{ row.nickname?.charAt(0) || row.username?.charAt(0) }}</el-avatar>
            <span>
              <strong>{{ row.nickname || row.username }}</strong>
              <small>{{ row.username }}</small>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="String(row.roles || '').includes('ADMIN') ? 'danger' : 'info'" size="small">
            {{ String(row.roles || '').includes('ADMIN') ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="校园认证" width="110">
        <template #default="{ row }">
          <el-tag :type="authType(row.campusAuthStatus)" size="small">{{ authText(row.campusAuthStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" width="100">
        <template #default="{ row }">{{ row.pointBalance ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="信任分" width="100">
        <template #default="{ row }">{{ Number(row.trustScore ?? 0).toFixed(1) }}</template>
      </el-table-column>
      <el-table-column label="校区" width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.campus || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row as UserAdminVo)">详情</el-button>
          <el-button size="small" @click="resetPassword(row as UserAdminVo)">重置密码</el-button>
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

    <el-drawer v-model="detailVisible" title="用户详情" size="460px">
      <div v-if="detail" class="detail-list">
        <p><span>用户</span>{{ detail.nickname || detail.username }}</p>
        <p><span>账号</span>{{ detail.username }}</p>
        <p><span>手机号</span>{{ detail.phone || '-' }}</p>
        <p><span>邮箱</span>{{ detail.email || '-' }}</p>
        <p><span>角色</span>{{ detail.roles || 'USER' }}</p>
        <p><span>积分</span>{{ detail.pointBalance ?? 0 }}</p>
        <p><span>信任分</span>{{ Number(detail.trustScore ?? 0).toFixed(1) }}</p>
        <p><span>校区</span>{{ detail.campus || '-' }}</p>
        <p><span>楼栋</span>{{ detail.building || '-' }}</p>
        <p><span>简介</span>{{ detail.bio || '-' }}</p>
      </div>
    </el-drawer>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserDetail, getAdminUserList, resetUserPassword, updateUserStatus } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import type { UserAdminVo } from '@/types/api'

const loading = ref(false)
const list = ref<UserAdminVo[]>([])
const detail = ref<UserAdminVo | null>(null)
const detailVisible = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(15)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

async function fetchData() {
  loading.value = true
  try {
    const result = await getAdminUserList({
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

function search() {
  page.value = 1
  fetchData()
}

function changePage(p: number) {
  page.value = p
  fetchData()
}

function authText(status?: number | null) {
  if (status === 1) return '已认证'
  if (status === 2) return '已拒绝'
  if (status === 0) return '待审核'
  return '未提交'
}

function authType(status?: number | null) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

async function showDetail(user: UserAdminVo) {
  try {
    detail.value = await getAdminUserDetail(user.id)
    detailVisible.value = true
  } catch {
    // handled
  }
}

async function resetPassword(user: UserAdminVo) {
  try {
    await ElMessageBox.confirm(`确认重置 ${user.nickname || user.username} 的密码？`, '重置密码', {
      type: 'warning',
      confirmButtonText: '重置',
      cancelButtonText: '取消',
    })
    const result = await resetUserPassword(user.id)
    ElMessageBox.alert(`新密码：${result.newPassword}`, '密码已重置')
  } catch {
    // cancelled or handled
  }
}

async function toggleStatus(user: UserAdminVo) {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      `确认${newStatus === 1 ? '启用' : '禁用'} ${user.nickname || user.username}？`,
      '账号状态',
      { type: 'warning' },
    )
    await updateUserStatus(user.id, newStatus)
    ElMessage.success('账号状态已更新')
    fetchData()
  } catch {
    // cancelled or handled
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  strong,
  small {
    display: block;
  }

  strong {
    color: #0f172a;
    font-size: 13px;
  }

  small {
    margin-top: 1px;
    color: #94a3b8;
    font-size: 11px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
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
    border-bottom: 1px solid #f1f5f9;
    font-size: 13px;
  }

  span {
    color: #94a3b8;
  }
}

:deep(.el-table) {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f0f9ff;
  font-size: 13px;
}

:deep(.el-table th) {
  font-weight: 600;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

:deep(.el-table td) {
  padding: 8px 0;
}
</style>
