<template>
  <view class="page-index">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <u-search
        v-model="keyword"
        placeholder="搜索技能服务..."
        :show-action="false"
        @search="onSearch"
        @clear="onClear"
      />
    </view>

    <!-- 分类标签 -->
    <scroll-view
      class="category-scroll"
      scroll-x
      :show-scrollbar="false"
    >
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="category-tag"
        :class="{ active: currentCatId === cat.id }"
        @tap="onCategoryTap(cat.id)"
      >
        {{ cat.name }}
      </view>
    </scroll-view>

    <!-- 货架列表 -->
    <view class="shelf-list">
      <view v-if="loading" class="loading-wrap">
        <u-loading mode="circle" size="40" />
        <text class="loading-text">加载中...</text>
      </view>

      <template v-else>
        <view
          v-for="item in shelfList"
          :key="item.id"
          class="shelf-card"
          @tap="goToShelf(item.id)"
        >
          <view class="card-header">
            <u-avatar :src="item.avatar" size="40" />
            <view class="user-info">
              <text class="nickname">{{ item.nickname }}</text>
              <text class="tag-name">{{ item.skillTagName }}</text>
            </view>
            <text class="price">{{ item.pointPrice }} 积分</text>
          </view>
          <text class="card-title">{{ item.title }}</text>
          <view class="card-meta">
            <text class="duration">⏱ {{ item.durationMinutes }}分钟</text>
            <text class="location">{{ locationName(item.locationType) }}</text>
          </view>
        </view>

        <u-empty v-if="shelfList.length === 0" text="暂无服务" />
      </template>
    </view>

    <!-- 加载更多 -->
    <view v-if="hasMore" class="load-more">
      <u-loadmore :status="loadStatus" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { getShelfList } from '@/services/shelf'
import { getCategories } from '@/services/shelf'
import { locationTypeName } from '@/utils/format'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()

const keyword = ref('')
const categories = ref([])
const currentCatId = ref(null)
const shelfList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)
const loadStatus = ref('loadmore')

onMounted(() => {
  loadCategories()
  loadShelves()
})

async function loadCategories() {
  if (appStore.categories.length > 0) {
    categories.value = appStore.categories
    return
  }
  try {
    const cats = await getCategories()
    categories.value = cats
    appStore.setCategories(cats)
  } catch (err) {
    console.error('load categories failed', err)
  }
}

async function loadShelves(reset) {
  if (loading.value) return
  if (reset) {
    pageNum.value = 1
    shelfList.value = []
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  loadStatus.value = 'loading'
  try {
    const result = await getShelfList({
      keyword: keyword.value || undefined,
      categoryId: currentCatId.value || undefined,
      pageNum: pageNum.value,
      pageSize: 10,
    })
    const list = result.list || result.records || []
    shelfList.value.push(...list)
    hasMore.value = pageNum.value * 10 < (result.total || 0)
    pageNum.value++
    loadStatus.value = hasMore.value ? 'loadmore' : 'nomore'
  } catch (err) {
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

// 触底加载更多
onReachBottom(() => loadShelves())

function onSearch() {
  loadShelves(true)
}

function onClear() {
  keyword.value = ''
  loadShelves(true)
}

function onCategoryTap(catId) {
  currentCatId.value = currentCatId.value === catId ? null : catId
  loadShelves(true)
}

function goToShelf(id) {
  uni.navigateTo({ url: '/pages/shelf-detail/shelf-detail?id=' + id })
}

function locationName(t) {
  return locationTypeName(t)
}
</script>

<style lang="scss" scoped>
.page-index {
  min-height: 100vh;
}

.search-bar {
  padding: 16rpx 24rpx;
  background: #fff;
}

.category-scroll {
  white-space: nowrap;
  padding: 16rpx 24rpx;
  background: #fff;

  .category-tag {
    display: inline-block;
    padding: 8rpx 24rpx;
    margin-right: 16rpx;
    font-size: 26rpx;
    border-radius: 30rpx;
    background: #f5f5f5;
    color: #666;

    &.active {
      background: #2979ff;
      color: #fff;
    }
  }
}

.shelf-list {
  padding: 16rpx 24rpx;
}

.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .loading-text {
    margin-top: 16rpx;
    color: #999;
    font-size: 28rpx;
  }
}

.shelf-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;

  .card-header {
    display: flex;
    align-items: center;

    .user-info {
      flex: 1;
      margin-left: 16rpx;

      .nickname {
        font-size: 28rpx;
        font-weight: 600;
        display: block;
      }

      .tag-name {
        font-size: 24rpx;
        color: #999;
      }
    }

    .price {
      color: #ff6348;
      font-size: 32rpx;
      font-weight: 600;
    }
  }

  .card-title {
    display: block;
    margin-top: 16rpx;
    font-size: 30rpx;
    color: #333;
  }

  .card-meta {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
    display: flex;
    gap: 24rpx;
  }
}

.load-more {
  padding: 24rpx 0;
}
</style>
