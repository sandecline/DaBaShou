/**
 * 日期工具函数
 * 原生实现，不依赖第三方库
 */

/**
 * 格式化日期
 * @param dateStr - 日期字符串或时间戳
 * @param format - 格式（默认 'YYYY-MM-DD HH:mm'）
 */
export function formatDate(dateStr: string | number, format = 'YYYY-MM-DD HH:mm'): string {
  const d = new Date(typeof dateStr === 'string' ? dateStr.replace(/-/g, '/') : dateStr);

  if (isNaN(d.getTime())) return '';

  const pad = (n: number) => String(n).padStart(2, '0');

  return format
    .replace('YYYY', String(d.getFullYear()))
    .replace('MM', pad(d.getMonth() + 1))
    .replace('DD', pad(d.getDate()))
    .replace('HH', pad(d.getHours()))
    .replace('mm', pad(d.getMinutes()))
    .replace('ss', pad(d.getSeconds()));
}

/**
 * 相对时间（如"刚刚"、"3分钟前"、"昨天"）
 */
export function timeAgo(dateStr: string | number): string {
  const d = new Date(typeof dateStr === 'string' ? dateStr.replace(/-/g, '/') : dateStr);
  const now = Date.now();
  const diff = now - d.getTime();

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return '刚刚';
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)}小时前`;
  if (diff < 2 * day) return '昨天';
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`;

  return formatDate(dateStr, 'MM-DD HH:mm');
}

/**
 * 倒计时
 * @param endTime - 截止时间
 * @returns { hours, minutes, seconds } 或 null（已过期）
 */
export function countdown(endTime: string): { hours: number; minutes: number; seconds: number } | null {
  const end = new Date(endTime.replace(/-/g, '/')).getTime();
  const now = Date.now();
  const diff = end - now;

  if (diff <= 0) return null;

  return {
    hours: Math.floor((diff / (1000 * 60 * 60)) % 24),
    minutes: Math.floor((diff / (1000 * 60)) % 60),
    seconds: Math.floor((diff / 1000) % 60),
  };
}

/**
 * 格式化倒计时为字符串
 */
export function countdownStr(endTime: string): string {
  const cd = countdown(endTime);
  if (!cd) return '已过期';

  const { hours, minutes, seconds } = cd;
  const parts: string[] = [];
  if (hours > 0) parts.push(`${hours}小时`);
  if (minutes > 0) parts.push(`${minutes}分`);
  parts.push(`${seconds}秒`);
  return parts.join('') + '后截止';
}

/**
 * 今天
 */
export function today(): string {
  return formatDate(Date.now(), 'YYYY-MM-DD');
}

/**
 * 一周后
 */
export function weekLater(): string {
  const d = new Date();
  d.setDate(d.getDate() + 7);
  return formatDate(d.getTime(), 'YYYY-MM-DD');
}
