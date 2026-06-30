import type { PageParams } from '@/types/api'

type CompatiblePageParams = PageParams & {
  page?: number
  size?: number
  sort?: string
  sortBy?: string
}

export function normalizePageParams<T extends CompatiblePageParams>(
  params?: T,
): PageParams & Omit<T, 'page' | 'size' | 'sort'> {
  const source = params || ({} as T)
  const { page, size, sort, ...rest } = source
  const normalized: Record<string, any> = {
    ...rest,
    pageNum: source.pageNum ?? page,
    pageSize: source.pageSize ?? size,
    sortBy: source.sortBy ?? sort,
  }

  Object.keys(normalized).forEach((key) => {
    if (normalized[key] === undefined || normalized[key] === null || normalized[key] === '') {
      delete normalized[key]
    }
  })

  return normalized as PageParams & Omit<T, 'page' | 'size' | 'sort'>
}
