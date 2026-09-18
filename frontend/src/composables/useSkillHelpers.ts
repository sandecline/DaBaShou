import { computed } from 'vue'

export function useUrgency(deadline: string | null | undefined) {
  const isUrgent = computed(() => {
    if (!deadline) return false
    const diff = new Date(deadline).getTime() - Date.now()
    return diff > 0 && diff < 12 * 3600 * 1000
  })
  return { isUrgent }
}

const CATEGORY_ICON_MAP: Record<string, string> = {
  '学业辅导': 'book',
  '编程开发': 'code',
  '生活服务': 'home',
  '设计创作': 'palette',
}

export function getCategoryIcon(name: string): string {
  for (const [key, icon] of Object.entries(CATEGORY_ICON_MAP)) {
    if (name.includes(key)) return icon
  }
  return 'star'
}
