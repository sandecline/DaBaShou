<template>
  <div class="appeal-page">
    <div class="page-container">
      <h2 class="page-title">申诉中心</h2>

      <!-- 发起申诉 -->
      <div class="appeal-form-card">
        <h3>发起申诉</h3>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="违规记录" prop="violationId">
            <el-select v-model="form.violationId" placeholder="选择要申诉的违规记录" style="width: 100%">
              <el-option
                v-for="v in violations"
                :key="v.id"
                :label="`${ViolationTypeMap[v.type]} - ${v.description}`"
                :value="v.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="申诉理由" prop="reason">
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细说明申诉理由..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="补充说明">
            <el-input
              v-model="form.evidence"
              type="textarea"
              :rows="3"
              placeholder="可以提供相关证据或说明（选填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ submitting ? '提交中...' : '提交申诉' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 申诉记录 -->
      <div class="appeal-list-card">
        <h3>申诉记录</h3>
        <el-table :data="appeals" stripe v-loading="appealLoading" empty-text="暂无申诉记录">
          <el-table-column prop="violationDesc" label="申诉事项" min-width="180" show-overflow-tooltip />
          <el-table-column prop="reason" label="申诉理由" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="appealStatusType(row.status)" size="small">
                {{ ['待审核', '已通过', '已驳回'][row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reply" label="处理回复" min-width="150" show-overflow-tooltip />
          <el-table-column label="提交时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyViolations, submitAppeal, getMyAppeals } from '@/api/credit'
import { formatDateTime } from '@/utils/format'
import { ViolationTypeMap } from '@/types/api'
import type { ViolationVo, AppealVo } from '@/types/api'

const formRef = ref()
const submitting = ref(false)
const appealLoading = ref(false)
const violations = ref<ViolationVo[]>([])
const appeals = ref<AppealVo[]>([])

const form = reactive({
  violationId: null as number | null,
  reason: '',
  evidence: '',
})

const rules = {
  violationId: [{ required: true, message: '请选择要申诉的记录', trigger: 'change' }],
  reason: [
    { required: true, message: '请填写申诉理由', trigger: 'blur' },
    { min: 10, message: '申诉理由至少10个字符', trigger: 'blur' },
  ],
}

function appealStatusType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitAppeal({
      violationId: form.violationId!,
      reason: form.reason,
      evidence: form.evidence,
    })
    ElMessage.success('申诉已提交，等待审核')
    form.violationId = null
    form.reason = ''
    form.evidence = ''
    loadAppeals()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

async function loadViolations() {
  try {
    const result = await getMyViolations({ page: 1, size: 50 })
    violations.value = result.list.filter((v) => v.status === 0)
  } catch {
    // handled
  }
}

async function loadAppeals() {
  appealLoading.value = true
  try {
    const result = await getMyAppeals({ page: 1, size: 50 })
    appeals.value = result.list
  } catch {
    // handled
  } finally {
    appealLoading.value = false
  }
}

onMounted(() => {
  loadViolations()
  loadAppeals()
})
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.appeal-form-card,
.appeal-list-card {
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  border: 1px solid $color-border;
  margin-bottom: $spacing-lg;

  h3 {
    margin: 0 0 $spacing-md;
    font-size: $font-size-lg;
    font-weight: 600;
  }
}
</style>
