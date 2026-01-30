import hamburgerImg from '@/assets/hamburger.png'

/**
 * 默认头像图片
 */
export const DEFAULT_AVATAR = hamburgerImg

/**
 * 默认封面图片
 */
export const DEFAULT_COVER = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

/**
 * 处理头像加载错误
 * @param event 图片错误事件
 */
export const handleAvatarError = (event: Event): void => {
  const img = event.target as HTMLImageElement
  img.src = DEFAULT_AVATAR
}

/**
 * 处理图片加载错误（隐藏图片）
 * @param event 图片错误事件
 */
export const handleImageError = (event: Event): void => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}
