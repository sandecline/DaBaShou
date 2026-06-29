<template>
  <div class="skill-list-page">
    <div class="page-container">
      <!-- 顶部操作栏 -->
      <div class="list-toolbar">
        <div class="toolbar-left">
          <h2>技能广场</h2>
          <el-button type="primary" @click="$router.push('/skill/publish')">
            <el-icon><Plus /></el-icon>
            发布技能
          </el-button>
        </div>

        <div class="toolbar-right">
          <el-input
            v-model="keyword"
            placeholder="搜索技能..."
            prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="search"
            @clear="search"
          />
        </div>
      </div>

      <!-- 分类筛选 -->
      <div class="category-filter">
        <el-radio-group v-model="selectedCategory" size="small" @change="search">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button
            v-for="cat in categories"
            :key="cat.id"
            :value="cat.id"
          >
            {{ cat.icon }} {{ cat.name }}
          </el-radio-button>
        </el-radio-group>

        <el-select
          v-model="sortBy"
          size="small"
          class="sort-select"
          @change="search"
        >
          <el-option label="按热度" value="heat" />
          <el-option label="按距离" value="distance" />
          <el-option label="按信任分" value="trust" />
          <el-option label="最新发布" value="latest" />
        </el-select>
      </div>

      <!-- 技能列表 -->
      <LoadingSpinner v-if="loading" text="加载中..." />
      <template v-else>
        <div v-if="list.length > 0" class="card-grid">
          <SkillCard v-for="skill in list" :key="skill.id" :skill="skill" />
        </div>
        <EmptyState v-else icon="🔍" title="没有找到相关技能" description="试试更换搜索条件或发布第一个技能吧">
          <el-button type="primary" @click="$router.push('/skill/publish')">发布技能</el-button>
        </EmptyState>
      </template>

      <!-- 分页 -->
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
import { useRoute } from 'vue-router'
import { getShelfList } from '@/api/shelf'
import { getCategories } from '@/api/skill'
import SkillCard from '@/components/common/SkillCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { SkillShelf, SkillCategory } from '@/types'

const route = useRoute()

const loading = ref(true)
const list = ref<SkillShelf[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const keyword = ref((route.query.keyword as string) || '')
const selectedCategory = ref(0)
const sortBy = ref('heat')
const categories = ref<SkillCategory[]>([])

async function fetchData() {
  loading.value = true
  try {
    const result = await getShelfList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      categoryId: selectedCategory.value || undefined,
      sort: sortBy.value,
    })
    list.value = result.records
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

onMounted(async () => {
  try {
    const cats = await getCategories()
    categories.value = cats.filter((c) => c.status === 1)
  } catch {
    // ignore
  }
  fetchData()
})
</script>

<style scoped lang="scss">
.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: 700;
    }
  }

  .search-input {
    width: 260px;
  }
}

.category-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
  flex-wrap: wrap;
  gap: 12px;

  .sort-select {
    width: 140px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xl;
}

@media (max-width: 768px) {
  .list-toolbar {
    flex-direction: column;
    align-items: flex-start;

    .search-input {
      width: 100%;
    }
  }
}
</style>
