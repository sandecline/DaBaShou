/**
 * 格式化工具
 */
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** 格式化时间：2026-01-01 00:00 */
export function formatTime(time) {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

/** 格式化日期：2026-01-01 */
export function formatDate(time) {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD')
}

/** 相对时间：3分钟前 / 昨天 */
export function fromNow(time) {
  if (!time) return ''
  return dayjs(time).fromNow()
}

/** 订单状态名称映射 */
export function orderStatusName(status) {
  const map = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '服务中',
    4: '待确认',
    5: '已完成',
    6: '已退款',
    7: '争议中',
  }
  return map[status] || '未知'
}

/** 地点类型描述 */
export function locationTypeName(t) {
  const map = { 1: '线上', 2: '线下', 3: '线上/线下' }
  return map[t] || '未知'
}

/** 技能熟练度描述 */
export function proficiencyName(p) {
  const map = { 1: '了解', 2: '熟悉', 3: '精通', 4: '专家' }
  return map[p] || '未知'
}
