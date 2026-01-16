import { useLoginUserStore } from '@/stores/loginUser'

/**
 * 权限校验工具
 * 用于检查用户是否有访问特定页面的权限
 */

/**
 * 检查用户是否为管理员
 * @param userRole 用户角色
 * @returns 是否为管理员
 */
export function isAdmin(userRole?: string): boolean {
  return userRole === 'admin' || userRole === '管理员'
}

/**
 * 检查用户是否已登录
 * @param loginUser 登录用户信息
 * @returns 是否已登录
 */
export function isLoggedIn(loginUser: API.LoginUserVO): boolean {
  return loginUser.id !== undefined && loginUser.id !== null
}

/**
 * 检查用户是否有访问管理员页面的权限
 * @param loginUser 登录用户信息
 * @returns 是否有权限
 */
export function canAccessAdmin(loginUser: API.LoginUserVO): boolean {
  return isLoggedIn(loginUser) && isAdmin(loginUser.userRole)
}

/**
 * 获取路由所需的权限
 * @param routePath 路由路径
 * @returns 所需权限类型：'admin' | 'user' | 'public'
 */
export function getRouteAccess(routePath: string): 'admin' | 'user' | 'public' {
  // 管理员页面需要 admin 权限
  if (routePath.startsWith('/admin')) {
    return 'admin'
  }
  
  // 用户相关页面需要登录（但不一定是管理员）
  if (routePath.startsWith('/user') && routePath !== '/user/login' && routePath !== '/user/register') {
    return 'user'
  }
  
  // 其他页面为公开访问
  return 'public'
}

/**
 * 检查用户是否有访问指定路由的权限
 * @param routePath 路由路径
 * @param loginUser 登录用户信息
 * @returns 是否有权限
 */
export function checkRouteAccess(routePath: string, loginUser: API.LoginUserVO): boolean {
  const access = getRouteAccess(routePath)
  
  switch (access) {
    case 'admin':
      return canAccessAdmin(loginUser)
    case 'user':
      return isLoggedIn(loginUser)
    case 'public':
      return true
    default:
      return false
  }
}
