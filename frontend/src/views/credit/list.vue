<template>
  <div class="credit-page">
    <div class="page-container">
      <h2 class="page-title">评价中心</h2>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我收到的评价" name="received" />
        <el-tab-pane label="我发出的评价" name="sent" />
      </el-tabs>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <div v-if="reviews.length > 0" class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-card">
            <div class="review-header">
              <div class="reviewer-info">
                <el-avatar :size="40" :src="activeTab === 'received' ? review.reviewerAvatar : review.reviewerAvatar">
                  {{ ((activeTab === 'received' ? review.reviewerName : review.revieweeName) || '?').charAt(0) }}
                </el-avatar>
                <div>
                  <div class="reviewer-name">
                    {{ activeTab === 'received' ? review.reviewerName : review.revieweeName }}
                  </div>
                  <div class="review-time">{{ formatDateTime(review.createTime) }}</div>
                </div>
              </div>
              <div class="review-rating">
                <el-rate v-model="review.rating" disabled show-score size="small" />
              </div>
            </div>
            <p class="review-content">{{ review.content || '用户未填写评价内容' }}</p>
            <div class="review-footer">
              <span class="review-order">订单：{{ review.orderTitle || '#' + review.orderId }}</span>
            </div>
          </div>
        </div>
        <EmptyState v-else icon="⭐" title="暂无评价" />
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
import { getMySentReviews } from '@/api/credit'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { ReviewVo } from '@/types/api'

const loading = ref(true)
const reviews = ref<ReviewVo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeTab = ref<'received' | 'sent'>('received')

async function fetchData() {
  loading.value = true
  try {
    const result = await getMySentReviews({
      page: page.value,
      size: size.value,
      type: activeTab.value,
    })
    reviews.value = result.list
    total.value = result.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
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
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-sm;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.review-card {
  background: #ffffff;
  border-radius: $radius-md;
  padding: $spacing-md;
  border: 1px solid $color-border;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .reviewer-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .reviewer-name { font-weight: 600; }
    .review-time { font-size: $font-size-xs; color: $color-text-placeholder; }
  }
}

.review-content {
  color: $color-text-regular;
  line-height: 1.6;
}

.review-footer {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid $color-border-light;

  .review-order {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}
</style>
