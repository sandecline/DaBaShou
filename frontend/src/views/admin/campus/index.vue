<template>
  <AdminLayout
    title="校园认证"
    eyebrow="IDENTITY REVIEW"
    subtitle="审核学生身份材料，通过后同步用户校区信息。"
  >
    <template #actions>
      <el-select v-model="statusFilter" placeholder="认证状态" clearable style="width: 140px" @change="search">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button :icon="Refresh" :loading="loading" @click="fetchData">刷新</el-button>
    </template>

    <el-table :data="list" stripe v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="用户" width="130" />
      <el-table-column prop="authType" label="认证方式" width="120" />
      <el-table-column prop="studentNo" label="学号" width="140" show-overflow-tooltip />
      <el-table-column prop="realName" label="真实姓名" width="110" />
      <el-table-column prop="campus" label="校区" width="130" />
      <el-table-column prop="college" label="学院" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="authTagType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row as CampusAuthAdminVo)">详情</el-button>
          <el-button v-if="row.status === 0" size="small" type="primary" @click="openReview(row as CampusAuthAdminVo)">审核</el-button>
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

    <el-drawer v-model="drawerVisible" title="认证详情" size="420px">
      <div v-if="current" class="detail-list">
        <p><span>用户</span>{{ current.nickname }}</p>
        <p><span>认证方式</span>{{ current.authType }}</p>
        <p><span>学号</span>{{ current.studentNo }}</p>
        <p><span>真实姓名</span>{{ current.realName }}</p>
        <p><span>校区</span>{{ current.campus }}</p>
        <p><span>学院</span>{{ current.college || '-' }}</p>
        <p><span>状态</span>{{ current.statusDesc }}</p>
        <p><span>审核备注</span>{{ current.reviewRemark || '-' }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="reviewDialog" title="审核校园认证" width="420px">
      <el-form label-position="top">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewApproved">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核说明">
          <el-input v-model="reviewReason" type="textarea" :rows="3" placeholder="拒绝时请填写原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">保存审核</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdminCampusAuths, reviewCampusAuth } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import type { CampusAuthAdminVo } from '@/types/api'

const loading = ref(false)
const reviewing = ref(false)
const list = ref<CampusAuthAdminVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const statusFilter = ref<number | undefined>(undefined)
const drawerVisible = ref(false)
const reviewDialog = ref(false)
const current = ref<CampusAuthAdminVo | null>(null)
const reviewApproved = ref(true)
const reviewReason = ref('')

async function fetchData() {
  loading.value = true
  try {
    const result = await getAdminCampusAuths({
      pageNum: page.value,
      pageSize: size.value,
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

function authTagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function openDetail(row: CampusAuthAdminVo) {
  current.value = row
  drawerVisible.value = true
}

function openReview(row: CampusAuthAdminVo) {
  current.value = row
  reviewApproved.value = true
  reviewReason.value = ''
  reviewDialog.value = true
}

async function submitReview() {
  if (!current.value) return
  if (!reviewApproved.value && !reviewReason.value.trim()) {
    ElMessage.warning('拒绝认证时请填写原因')
    return
  }
  reviewing.value = true
  try {
    await reviewCampusAuth(current.value.id, {
      approved: reviewApproved.value,
      reason: reviewReason.value.trim(),
    })
    ElMessage.success('认证审核已保存')
    reviewDialog.value = false
    fetchData()
  } catch {
    // handled
  } finally {
    reviewing.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.detail-list {
  display: grid;
  gap: 12px;

  p {
    display: grid;
    grid-template-columns: 88px minmax(0, 1fr);
    gap: 10px;
    margin: 0;
    padding-bottom: 12px;
    border-bottom: 1px solid #edf2ef;
    color: #17211f;
  }

  span {
    color: #667a74;
  }
}
</style>
