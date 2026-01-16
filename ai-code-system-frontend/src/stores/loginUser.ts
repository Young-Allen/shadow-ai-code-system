import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController'
import ACCESS_ENUM from '@/access/accessEnum'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
    userRole: ACCESS_ENUM.NOT_LOGIN,
  } as any)

  // 是否已经向后端拉取过一次登录用户
  const firstFetchLoginUser = ref(false)

  const fetchLoginUser = async () => {
    if (firstFetchLoginUser.value) return

    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      } else {
        // ✅ 未登录也要标记为“已拉取过”
        loginUser.value = {
          userName: '未登录',
          userRole: ACCESS_ENUM.NOT_LOGIN,
        } as any
      }
    } catch (e) {
      console.error('获取登录用户信息失败:', e)
      loginUser.value = {
        userName: '未登录',
        userRole: ACCESS_ENUM.NOT_LOGIN,
      } as any
    } finally {
      // ✅ 不管成功失败，都标记一次 fetch 已完成
      firstFetchLoginUser.value = true
    }
  }

  const refreshLoginUser = async () => {
    firstFetchLoginUser.value = false
    await fetchLoginUser()
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
    firstFetchLoginUser.value = true
  }

  function resetLoginUser() {
    loginUser.value = {
      userName: '未登录',
      userRole: ACCESS_ENUM.NOT_LOGIN,
    } as any
    firstFetchLoginUser.value = true
  }

  return {
    loginUser,
    firstFetchLoginUser,
    fetchLoginUser,
    refreshLoginUser,
    setLoginUser,
    resetLoginUser,
  }
})
