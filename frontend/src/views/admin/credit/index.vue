<template>
  <AdminLayout
    title="信用审核"
    eyebrow="TRUST REVIEW"
    subtitle="处理违规记录、用户申诉和不当评价，维护平台信任分体系。"
  >
    <template #actions>
      <el-button :icon="Refresh" :loading="currentLoading" @click="reloadCurrent">刷新</el-button>
    </template>

    <el-tabs v-model="activeTab" @tab-change="reloadCurrent">
      <el-tab-pane label="违规记录" name="violations" />
      <el-tab-pane label="申诉审核" name="appeals" />
      <el-tab-pane label="评价管理" name="reviews" />
    </el-tabs>

    <el-table v-if="activeTab === 'violations'" :data="violations" stripe v-loading="violationsLoading" border>
      <el-table-column prop="targetNickname" label="违规用户" width="130" />
      <el-table-column prop="reporterNickname" label="举报人" width="130" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag type="danger" size="small">{{ row.typeDesc || ViolationTypeMap[row.type as ViolationType] || row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'info'" size="small">
            {{ row.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openViolation(row as ViolationRow, 'dismiss')">撤销</el-button>
          <el-button v-if="row.status === 0" size="small" type="warning" @click="openViolation(row as ViolationRow, 'confirm')">确认违规</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-if="activeTab === 'appeals'" :data="appeals" stripe v-loading="appealsLoading" border>
      <el-table-column prop="appellantNickname" label="申诉人" width="130" />
      <el-table-column prop="reason" label="申诉理由" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="appealTagType(row.status)" size="small">{{ appealStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewRemark" label="审核说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="提交时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="openAppeal(row as AppealRow, true)">通过</el-button>
            <el-button size="small" type="danger" @click="openAppeal(row as AppealRow, false)">驳回</el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-if="activeTab === 'reviews'" :data="reviews" stripe v-loading="reviewsLoading" border>
      <el-table-column prop="orderTitle" label="订单" min-width="170" show-overflow-tooltip />
      <el-table-column prop="reviewerName" label="评价人" width="120" />
      <el-table-column prop="revieweeName" label="被评价人" width="120" />
      <el-table-column label="评分" width="140">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="hideReview(row as ReviewVo)">隐藏</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="violationDialog" title="处理违规记录" width="420px">
      <el-form label-position="top">
        <el-form-item label="处理结果">
          <el-input :model-value="violationAction === 'confirm' ? '确认违规并扣减信任分' : '撤销违规记录'" disabled />
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="violationReason" type="textarea" :rows="3" placeholder="说明处理依据" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="violationDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitViolation">保存处理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="appealDialog" title="审核申诉" width="420px">
      <el-form label-position="top">
        <el-form-item label="审核结果">
          <el-input :model-value="appealApproved ? '通过申诉' : '驳回申诉'" disabled />
        </el-form-item>
        <el-form-item label="审核说明">
          <el-input v-model="appealReason" type="textarea" :rows="3" placeholder="驳回时请填写原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAppeal">保存审核</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminHandleAppeal,
  adminHandleViolation,
  deleteReview,
  getAdminAppeals,
  getAdminReviews,
  getAdminViolations,
} from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { ViolationTypeMap } from '@/types/api'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import type { AppealVo, ReviewVo, ViolationType, ViolationVo } from '@/types/api'

type AppealRow = AppealVo & { appellantNickname?: string; reviewRemark?: string }
type ViolationRow = ViolationVo & { reporterNickname?: string; typeDesc?: string }

const activeTab = ref<'violations' | 'appeals' | 'reviews'>('violations')
const violations = ref<ViolationRow[]>([])
const appeals = ref<AppealRow[]>([])
const reviews = ref<ReviewVo[]>([])
const violationsLoading = ref(false)
const appealsLoading = ref(false)
const reviewsLoading = ref(false)
const submitting = ref(false)
const violationDialog = ref(false)
const appealDialog = ref(false)
const currentViolation = ref<ViolationRow | null>(null)
const currentAppeal = ref<AppealRow | null>(null)
const violationAction = ref<'confirm' | 'dismiss'>('confirm')
const appealApproved = ref(true)
const violationReason = ref('')
const appealReason = ref('')

const currentLoading = computed(() => {
  if (activeTab.value === 'violations') return violationsLoading.value
  if (activeTab.value === 'appeals') return appealsLoading.value
  return reviewsLoading.value
})

async function loadViolations() {
  violationsLoading.value = true
  try {
    const result = await getAdminViolations({ pageNum: 1, pageSize: 50 })
    violations.value = result.list as ViolationRow[]
  } catch {
    // handled
  } finally {
    violationsLoading.value = false
  }
}

async function loadAppeals() {
  appealsLoading.value = true
  try {
    const result = await getAdminAppeals({ pageNum: 1, pageSize: 50 })
    appeals.value = result.list as AppealRow[]
  } catch {
    // handled
  } finally {
    appealsLoading.value = false
  }
}

async function loadReviews() {
  reviewsLoading.value = true
  try {
    const result = await getAdminReviews({ pageNum: 1, pageSize: 50 })
    reviews.value = result.list
  } catch {
    // handled
  } finally {
    reviewsLoading.value = false
  }
}

function reloadCurrent() {
  if (activeTab.value === 'violations') loadViolations()
  if (activeTab.value === 'appeals') loadAppeals()
  if (activeTab.value === 'reviews') loadReviews()
}

function appealStatusText(status: number) {
  return ['待审核', '已通过', '已驳回'][status] || '未知'
}

function appealTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function openViolation(row: ViolationRow, action: 'confirm' | 'dismiss') {
  currentViolation.value = row
  violationAction.value = action
  violationReason.value = action === 'confirm' ? '管理员确认违规' : '管理员撤销违规'
  violationDialog.value = true
}

function openAppeal(row: AppealRow, approved: boolean) {
  currentAppeal.value = row
  appealApproved.value = approved
  appealReason.value = approved ? '申诉通过' : ''
  appealDialog.value = true
}

async function submitViolation() {
  if (!currentViolation.value) return
  if (!violationReason.value.trim()) {
    ElMessage.warning('请填写处理说明')
    return
  }
  submitting.value = true
  try {
    await adminHandleViolation(currentViolation.value.id, `${violationAction.value}:${violationReason.value.trim()}`)
    ElMessage.success('违规记录已处理')
    violationDialog.value = false
    loadViolations()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

async function submitAppeal() {
  if (!currentAppeal.value) return
  if (!appealApproved.value && !appealReason.value.trim()) {
    ElMessage.warning('驳回申诉时请填写原因')
    return
  }
  submitting.value = true
  try {
    await adminHandleAppeal(currentAppeal.value.id, {
      approved: appealApproved.value,
      reason: appealReason.value.trim() || '申诉通过',
    })
    ElMessage.success('申诉审核已保存')
    appealDialog.value = false
    loadAppeals()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

async function hideReview(row: ReviewVo) {
  try {
    await ElMessageBox.confirm('确认隐藏这条评价？', '隐藏评价', { type: 'warning' })
    await deleteReview(row.id)
    ElMessage.success('评价已隐藏')
    loadReviews()
  } catch {
    // cancelled or handled
  }
}

onMounted(() => {
  loadViolations()
  loadAppeals()
  loadReviews()
})
</script>
