<template>
  <div class="skill-publish-page">
    <div class="page-container">
      <el-button text @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>

      <div class="publish-card">
        <h2>{{ isEdit ? '编辑技能服务' : '发布技能服务' }}</h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
        >
          <!-- 技能分类 -->
          <el-form-item label="技能分类" prop="skillTagId">
            <el-cascader
              v-model="cascaderValue"
              :options="cascaderOptions"
              :props="{ value: 'id', label: 'name', emitPath: false }"
              placeholder="请选择技能分类"
              style="width: 100%"
              @change="handleCascaderChange"
            />
          </el-form-item>

          <!-- 服务标题 -->
          <el-form-item label="服务标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="简洁明了地描述你的服务，如：『PPT排版美化』"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <!-- 服务描述 -->
          <el-form-item label="服务描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="详细描述你的服务内容、特点和注意事项..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 价格 & 时长 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="积分价格" prop="pointPrice">
                <el-input-number
                  v-model="form.pointPrice"
                  :min="1"
                  :max="9999"
                  :step="10"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预计时长（分钟）" prop="durationMinutes">
                <el-input-number
                  v-model="form.durationMinutes"
                  :min="15"
                  :max="480"
                  :step="15"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 服务方式 -->
          <el-form-item label="服务方式" prop="locationType">
            <el-radio-group v-model="form.locationType">
              <el-radio :value="1">线上服务</el-radio>
              <el-radio :value="2">线下服务</el-radio>
              <el-radio :value="3">均可</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 闲时格子 -->
          <el-form-item label="空闲时段">
            <TimeSlotPicker v-model="form.timeSlots" />
          </el-form-item>

          <!-- 提交 -->
          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              size="large"
              round
              class="submit-btn"
              @click="handleSubmit"
            >
              {{ submitting ? '发布中...' : isEdit ? '保存修改' : '立即发布' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishShelf } from '@/api/shelf'
import { getCategoryTree, getTags } from '@/api/skill'
import TimeSlotPicker from '@/components/common/TimeSlotPicker.vue'
import type { SkillTagVo } from '@/types/api'

const router = useRouter()
const isEdit = ref(false)

const formRef = ref()
const submitting = ref(false)
const cascaderValue = ref<number | null>(null)
const cascaderOptions = ref<Array<{ id: number; name: string; children: Array<{ id: number; name: string }> }>>([])

const form = reactive<{
  skillTagId: number | null
  title: string
  description: string
  pointPrice: number
  durationMinutes: number
  locationType: number
  timeSlots: Array<{ date: string; startTime: string; endTime: string }>
}>({
  skillTagId: null,
  title: '',
  description: '',
  pointPrice: 100,
  durationMinutes: 60,
  locationType: 1,
  timeSlots: [],
})

const rules = {
  skillTagId: [{ required: true, message: '请选择技能分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入服务标题', trigger: 'blur' },
    { min: 4, max: 100, message: '标题4-100个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述10-500个字符', trigger: 'blur' },
  ],
  pointPrice: [{ required: true, message: '请设置积分价格', trigger: 'change' }],
  durationMinutes: [{ required: true, message: '请设置预计时长', trigger: 'change' }],
  locationType: [{ required: true, message: '请选择服务方式', trigger: 'change' }],
}

function handleCascaderChange(value: unknown) {
  const selected = Array.isArray(value) ? value[value.length - 1] : value
  form.skillTagId = selected == null ? null : Number(selected)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await publishShelf({ ...form })
    ElMessage.success(isEdit.value ? '修改成功！' : '发布成功！🎉')
    router.push('/user/shop')
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const [categories, tags] = await Promise.all([
      getCategoryTree(),
      getTags(),
    ])

    cascaderOptions.value = categories
      .filter((c: any) => c.id)
      .map((cat) => ({
        id: cat.id,
        name: cat.name,
        children: tags
          .filter((t) => t.categoryId === cat.id && t.status === 1)
          .map((t) => ({ id: t.id, name: t.name })),
      }))
  } catch {
    // ignore
  }
})
</script>

<style scoped lang="scss">
.back-btn {
  margin-bottom: $spacing-md;
}

.publish-card {
  max-width: 720px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border;

  h2 {
    margin: 0 0 $spacing-xl;
    font-size: $font-size-xl;
    font-weight: 700;
  }
}

.submit-btn {
  width: 100%;
  margin-top: $spacing-md;
}
</style>
