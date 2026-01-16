import router from '@/router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  // 1) 自动登录：如果之前没获取过，就先拉一次（只会请求一次）
  if (!loginUserStore.firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
  }

  // 2) 读取路由所需权限（默认公开）
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  // 3) 需要登录但未登录：跳转登录页 + 带 redirect
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    if (!loginUser || !loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      message.warning('请先登录')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }

    // 4) 已登录但权限不足：跳无权限页
    if (!checkAccess(loginUser, needAccess as any)) {
      message.error('没有权限')
      next('/noAuth')
      return
    }
  }

  next()
})
