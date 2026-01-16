import ACCESS_ENUM, { type AccessType } from './accessEnum'

/**
 * 检查权限（判断当前登录用户是否具有某个权限）
 * @param loginUser 当前登录用户
 * @param needAccess 需要的权限
 * @returns boolean 有无权限
 */
const checkAccess = (loginUser: any, needAccess: AccessType = ACCESS_ENUM.NOT_LOGIN) => {
  // 当前用户拥有的权限（没有 loginUser 则视为未登录）
  const loginUserAccess: AccessType = (loginUser?.userRole as AccessType) ?? ACCESS_ENUM.NOT_LOGIN

  // 公开页面：任何人可访问
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true
  }

  // 需要登录：只要不是未登录即可
  if (needAccess === ACCESS_ENUM.USER) {
    return loginUserAccess !== ACCESS_ENUM.NOT_LOGIN
  }

  // 需要管理员：必须是 admin
  if (needAccess === ACCESS_ENUM.ADMIN) {
    return loginUserAccess === ACCESS_ENUM.ADMIN
  }

  return true
}

export default checkAccess
