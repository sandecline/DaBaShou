<template>
  <AdminLayout
    title="系统配置"
    eyebrow="RULE SETTINGS"
    subtitle="调整积分、信任分、超时熔断和退改规则。"
  >
    <template #actions>
      <el-button :loading="loading" @click="loadConfig">重新读取</el-button>
      <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
    </template>

    <LoadingSpinner v-if="loading" text="加载配置..." />

    <div v-else class="config-grid">
      <section class="config-panel">
        <h2>站点信息</h2>
        <el-form label-position="top">
          <el-form-item label="平台名称">
            <el-input v-model="config['site.name']" placeholder="搭把手" />
          </el-form-item>
          <el-form-item label="运营公告">
            <el-input v-model="config['site.notice']" type="textarea" :rows="3" placeholder="展示给管理员的运营备注" />
          </el-form-item>
        </el-form>
      </section>

      <section class="config-panel">
        <h2>积分规则</h2>
        <el-form label-position="top">
          <el-form-item label="注册送积分">
            <el-input-number v-model="config['point.register_bonus']" :min="0" :max="10000" />
          </el-form-item>
          <el-form-item label="签到奖励">
            <el-input-number v-model="config['point.sign_in_reward']" :min="0" :max="100" />
          </el-form-item>
        </el-form>
      </section>

      <section class="config-panel">
        <h2>信任分规则</h2>
        <el-form label-position="top">
          <el-form-item label="新人上限">
            <el-input-number v-model="config['credit.newcomer_max']" :min="0" :max="5" :precision="1" :step="0.1" />
          </el-form-item>
          <el-form-item label="靠谱上限">
            <el-input-number v-model="config['credit.reliable_max']" :min="0" :max="5" :precision="1" :step="0.1" />
          </el-form-item>
          <el-form-item label="违规扣分">
            <el-input-number v-model="config['credit.violation_penalty']" :min="0" :max="5" :precision="1" :step="0.1" />
          </el-form-item>
        </el-form>
      </section>

      <section class="config-panel">
        <h2>履约熔断</h2>
        <el-form label-position="top">
          <el-form-item label="待支付超时（分钟）">
            <el-input-number v-model="config['order.auto_cancel_minutes']" :min="1" :max="180" />
          </el-form-item>
          <el-form-item label="核销码有效期（分钟）">
            <el-input-number v-model="config['order.verify_code_minutes']" :min="1" :max="180" />
          </el-form-item>
          <el-form-item label="服务确认超时（小时）">
            <el-input-number v-model="config['order.confirm_timeout_hours']" :min="1" :max="720" />
          </el-form-item>
        </el-form>
      </section>

      <section class="config-panel">
        <h2>退改扣除</h2>
        <el-form label-position="top">
          <el-form-item label="买家取消扣除比例（%）">
            <el-input-number v-model="config['order.buyer_cancel_penalty']" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="卖家取消扣除比例（%）">
            <el-input-number v-model="config['order.seller_cancel_penalty']" :min="0" :max="100" />
          </el-form-item>
        </el-form>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfig, updateSystemConfig } from '@/api/admin'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const loading = ref(false)
const saving = ref(false)
const config = reactive<Record<string, any>>({
  'site.name': '搭把手',
  'site.notice': '',
  'point.register_bonus': 100,
  'point.sign_in_reward': 5,
  'credit.newcomer_max': 2.9,
  'credit.reliable_max': 3.9,
  'credit.violation_penalty': 0.5,
  'order.auto_cancel_minutes': 15,
  'order.verify_code_minutes': 30,
  'order.confirm_timeout_hours': 168,
  'order.buyer_cancel_penalty': 10,
  'order.seller_cancel_penalty': 20,
})

const numericKeys = new Set([
  'point.register_bonus',
  'point.sign_in_reward',
  'credit.newcomer_max',
  'credit.reliable_max',
  'credit.violation_penalty',
  'order.auto_cancel_minutes',
  'order.verify_code_minutes',
  'order.confirm_timeout_hours',
  'order.buyer_cancel_penalty',
  'order.seller_cancel_penalty',
])

async function loadConfig() {
  loading.value = true
  try {
    const result = await getSystemConfig()
    Object.entries(result || {}).forEach(([key, value]) => {
      config[key] = numericKeys.has(key) ? Number(value) : value
    })
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function validateConfig() {
  if (Number(config['credit.newcomer_max']) >= Number(config['credit.reliable_max'])) {
    ElMessage.warning('新人上限必须小于靠谱上限')
    return false
  }
  return true
}

async function saveConfig() {
  if (!validateConfig()) return
  saving.value = true
  try {
    await updateSystemConfig({ ...config })
    ElMessage.success('系统配置已保存')
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

loadConfig()
</script>

<style scoped lang="scss">
.config-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.config-panel {
  border: 1px solid #dfe7e3;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;

  h2 {
    margin: 0 0 14px;
    color: #17211f;
    font-size: 17px;
  }
}

@media (max-width: 900px) {
  .config-grid {
    grid-template-columns: 1fr;
  }
}
</style>
