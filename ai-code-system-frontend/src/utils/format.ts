/**
 * 格式化日期时间
 * @param dateStr 日期字符串
 * @returns 格式化后的日期时间字符串，格式：YYYY-MM-DD HH:mm
 */
export const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 格式化相对时间
 * @param timestamp 时间戳（毫秒）
 * @returns 相对时间字符串，如：刚刚、1小时前、1天前、1周前
 */
export const formatTime = (timestamp?: number): string => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)
  const weeks = Math.floor(days / 7)

  if (weeks > 0) {
    return `${weeks}周前`
  } else if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else {
    return '刚刚'
  }
}

/**
 * 格式化创建时间（用于首页）
 * @param timeStr 时间字符串
 * @returns 格式化后的创建时间字符串
 */
export const formatCreateTime = (timeStr?: string): string => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)
  const weeks = Math.floor(days / 7)

  if (weeks > 0) {
    return `创建于${weeks}周前`
  } else if (days > 0) {
    return `创建于${days}天前`
  } else if (hours > 0) {
    return `创建于${hours}小时前`
  } else {
    return '刚刚创建'
  }
}
