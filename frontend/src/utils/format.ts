import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import type { OrderStatus } from '@/types'
import { OrderStatusMap, OrderStatusColor } from '@/types'
import type { TrustLevel } from '@/types'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** 格式化日期时间 */
export function formatDateTime(date: string | Date, format = 'YYYY-MM-DD HH:mm'): string {
  return dayjs(date).format(format)
}

/** 格式化日期 */
export function formatDate(date: string | Date, format = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/** 相对时间（如"3小时前"） */
export function fromNow(date: string | Date): string {
  return dayjs(date).fromNow()
}

/** 格式化积分（带+/-号） */
export function formatPoints(amount: number): string {
  if (amount > 0) return `+${amount}`
  return `${amount}`
}

/** 获取订单状态文本 */
export function getOrderStatusText(status: OrderStatus): string {
  return OrderStatusMap[status] || '未知'
}

/** 获取订单状态颜色 */
export function getOrderStatusColor(status: OrderStatus): string {
  return OrderStatusColor[status] || '#909399'
}

/** 根据信任分获取等级 */
export function getTrustLevel(score: number): { level: TrustLevel; label: string; color: string } {
  if (score >= 4.0) return { level: 'gold', label: '金牌', color: '#f59e0b' }
  if (score >= 3.0) return { level: 'reliable', label: '靠谱', color: '#FF8F00' }
  return { level: 'newcomer', label: '新人', color: '#10b981' }
}

/** 格式化距离 */
export function formatDistance(meters: number): string {
  if (meters < 1000) return `${Math.round(meters)}m`
  return `${(meters / 1000).toFixed(1)}km`
}

/** 格式化倒计时 */
export function formatCountdown(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/** 截断文本 */
export function truncate(text: string, length: number): string {
  if (text.length <= length) return text
  return text.slice(0, length) + '...'
}
