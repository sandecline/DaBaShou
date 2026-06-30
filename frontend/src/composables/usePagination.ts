import { ref, computed } from 'vue'
import type { Ref } from 'vue'
import type { PageParams, PageResult } from '@/types'

export function usePagination<T>(
  fetchFn: (params: PageParams) => Promise<PageResult<T>>,
  defaultSize = 12,
) {
  const loading = ref(false)
  const list: Ref<T[]> = ref([]) as Ref<T[]>
  const total = ref(0)
  const page = ref(1)
  const size = ref(defaultSize)
  const noMore = computed(() => list.value.length >= total.value)
  const isEmpty = computed(() => !loading.value && list.value.length === 0)

  async function loadData(reset = false) {
    if (reset) {
      page.value = 1
      list.value = []
    }
    loading.value = true
    try {
      const result = await fetchFn({ pageNum: page.value, pageSize: size.value })
      const records = result.records || result.list || []
      if (reset) {
        list.value = records
      } else {
        list.value.push(...records)
      }
      total.value = result.total
    } catch {
      // handled by request interceptor
    } finally {
      loading.value = false
    }
  }

  async function loadMore() {
    if (loading.value || noMore.value) return
    page.value++
    await loadData()
  }

  async function refresh() {
    await loadData(true)
  }

  async function changePage(newPage: number) {
    page.value = newPage
    await loadData(true)
  }

  return {
    loading,
    list,
    total,
    page,
    size,
    noMore,
    isEmpty,
    loadData,
    loadMore,
    refresh,
    changePage,
  }
}
