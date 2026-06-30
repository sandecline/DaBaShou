<template>
  <div class="demand-publish-page">
    <div class="page-container">
      <el-button text @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>

      <div class="publish-card">
        <h2>发布求助需求</h2>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
        >
          <el-form-item label="需求标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="简洁描述你的需求，如：『急求明天下午帮忙搬宿舍』"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="需求描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="详细说明你需要什么帮助、具体要求、时间安排等..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="悬赏积分" prop="pointReward">
                <el-input-number
                  v-model="form.pointReward"
                  :min="10"
                  :max="9999"
                  :step="10"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="截止时间" prop="deadline">
                <el-date-picker
                  v-model="form.deadline"
                  type="datetime"
                  placeholder="选择截止时间（可不选）"
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="服务方式" prop="locationType">
            <el-radio-group v-model="form.locationType">
              <el-radio :value="1">线上</el-radio>
              <el-radio :value="2">线下</el-radio>
              <el-radio :value="3">均可</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="需求分类（选填）">
            <el-select v-model="form.skillTagId" placeholder="选择所需技能分类" clearable style="width: 100%">
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="form.isUrgent">标记为急单（12小时内加急完成）</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              size="large"
              round
              class="submit-btn"
              @click="handleSubmit"
            >
              {{ submitting ? '发布中...' : '发布求助（消耗 ' + form.pointReward + ' 积分）' }}
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
import { publishDemand } from '@/api/demand'
import { getTags } from '@/api/skill'
import type { SkillTagVo, DemandForm } from '@/types/api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const tags = ref<SkillTagVo[]>([])

const form = reactive<DemandForm>({
  skillTagId: null,
  title: '',
  description: '',
  pointReward: 50,
  deadline: null,
  locationType: 1,
  isUrgent: false,
})

const rules = {
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 4, max: 100, message: '标题4-100个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入需求描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述10-500个字符', trigger: 'blur' },
  ],
  pointReward: [{ required: true, message: '请设置悬赏积分', trigger: 'change' }],
  locationType: [{ required: true, message: '请选择服务方式', trigger: 'change' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await publishDemand({ ...form })
    ElMessage.success('求助发布成功！等待热心同学接单 🎉')
    router.push('/demand')
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const result = await getTags()
    tags.value = result.filter((t) => t.status === 1)
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
