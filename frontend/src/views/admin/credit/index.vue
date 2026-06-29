<template>
  <div class="admin-page">
    <div class="admin-layout">
      <Sidebar :menu-items="adminMenu" />
      <div class="admin-content">
        <div class="page-container">
          <h2>信用管理</h2>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="违规记录" name="violations" />
            <el-tab-pane label="申诉审核" name="appeals" />
          </el-tabs>

          <!-- 违规记录 -->
          <template v-if="activeTab === 'violations'">
            <el-table :data="violations" stripe v-loading="vLoading" border>
              <el-table-column prop="userName" label="用户" width="120" />
              <el-table-column label="违规类型" width="120">
                <template #default="{ row }">
                  <el-tag type="danger" size="small">{{ ViolationTypeMap[row.type as ViolationType] }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="penaltyPoints" label="扣分" width="80" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 0 ? 'warning' : 'info'" size="small">
                    {{ row.status === 0 ? '未处理' : '已处理' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <template v-if="row.status === 0">
                    <el-button size="small" type="success" @click="handleViolationClick(row, 'confirm')">确认违规</el-button>
                    <el-button size="small" @click="handleViolationClick(row, 'dismiss')">撤销</el-button>
                  </template>
                  <span v-else>-</span>
                </template>
              </el-table-column>
            </el-table>
          </template>

          <!-- 申诉审核 -->
          <template v-if="activeTab === 'appeals'">
            <el-table :data="appeals" stripe v-loading="aLoading" border>
              <el-table-column prop="userName" label="申诉人" width="100" />
              <el-table-column prop="reason" label="申诉理由" min-width="200" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="['warning', 'success', 'danger'][row.status] as any" size="small">
                    {{ ['待审核', '已通过', '已驳回'][row.status] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <template v-if="row.status === 0">
                    <el-button size="small" type="success" @click="handleAppealClick(row, 'approve')">通过</el-button>
                    <el-button size="small" type="danger" @click="handleAppealClick(row, 'reject')">驳回</el-button>
                  </template>
                  <span v-else>{{ row.reply || '-' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminViolations, handleViolation, getAdminAppeals, handleAppeal } from '@/api/admin'
import { ViolationTypeMap } from '@/types'
import Sidebar from '@/components/layout/Sidebar.vue'
import type { Violation, Appeal, ViolationType } from '@/types'

const adminMenu = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Document' },
  { path: '/admin/credit', title: '信用管理', icon: 'Warning' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const activeTab = ref('violations')
const violations = ref<Violation[]>([])
const appeals = ref<Appeal[]>([])
const vLoading = ref(false)
const aLoading = ref(false)

async function loadViolations() {
  vLoading.value = true
  try {
    const result = await getAdminViolations({ page: 1, size: 50 })
    violations.value = result.records
  } catch { /* handled */ } finally { vLoading.value = false }
}

async function loadAppeals() {
  aLoading.value = true
  try {
    const result = await getAdminAppeals({ page: 1, size: 50 })
    appeals.value = result.records
  } catch { /* handled */ } finally { aLoading.value = false }
}

async function handleViolationClick(row: Violation, action: 'confirm' | 'dismiss') {
  try {
    await handleViolation(row.id, action)
    ElMessage.success(action === 'confirm' ? '已确认违规' : '已撤销')
    loadViolations()
  } catch { /* handled */ }
}

async function handleAppealClick(row: Appeal, action: 'approve' | 'reject') {
  try {
    const { value: reply } = action === 'reject'
      ? await ElMessageBox.prompt('请输入驳回理由', '驳回申诉')
      : { value: undefined }
    await handleAppeal(row.id, action, reply || undefined)
    ElMessage.success(action === 'approve' ? '申诉已通过' : '申诉已驳回')
    loadAppeals()
  } catch { /* cancelled */ }
}

onMounted(() => {
  loadViolations()
  loadAppeals()
})
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
</style>
